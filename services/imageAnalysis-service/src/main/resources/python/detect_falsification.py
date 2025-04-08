import argparse
import json
import torch
import torch.nn as nn
import numpy as np
import sys
from pathlib import Path
from PIL import Image, UnidentifiedImageError
import logging
import timm
from transformers import ViTModel
import albumentations as A
from albumentations.pytorch import ToTensorV2

logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(levelname)s - %(message)s',
    datefmt='%Y-%m-%d %H:%M:%S',
    handlers=[logging.StreamHandler(sys.stdout)],
    force=True
)


class CustomModel(nn.Module):
    def __init__(self, model_name, dense_units, dropout, pretrained=True, unfreeze_layers=0):
        super(CustomModel, self).__init__()
        self.model_name = model_name
        self.base_model = None
        reported_features = 0

        logging.debug(
            f"Initializing CustomModel: Base='{model_name}', DenseUnits={dense_units}, Dropout={dropout:.2f}, Unfreeze={unfreeze_layers}")

        try:
            if model_name == "ViT_Base":
                hf_model_name = 'google/vit-base-patch16-224'
                self.base_model = ViTModel.from_pretrained(
                    hf_model_name,
                    add_pooling_layer=False,
                )
                reported_features = self.base_model.config.hidden_size
            else:
                timm_model_name_map = {
                    "ConvNeXt_Base": "convnext_base.fb_in22k_ft_in1k",
                    "EfficientNetV2-S": "tf_efficientnetv2_s.in21k_ft_in1k",
                    "DeiT_Base": "deit_base_patch16_224.fb_in1k",
                    "BEiT_Base": "beit_base_patch16_224",
                    "EfficientNet_B7": "tf_efficientnet_b7.ns_jft_in1k",
                    "ResNetRS50": "resnetrs50.tf_in1k",
                    "InceptionV3": "inception_v3.tf_in1k",
                    "Xception": "xception.tf_in1k",
                    "MobileNetV3_Large": "mobilenetv3_large_100.miil_in21k_ft_in1k",
                }
                if model_name not in timm_model_name_map:
                    raise ValueError(f"Model name '{model_name}' not found in timm map or not supported.")

                timm_name = timm_model_name_map[model_name]
                self.base_model = timm.create_model(timm_name, pretrained=pretrained, num_classes=0)
                reported_features = self.base_model.num_features

            try:
                self.base_model.eval()
                with torch.no_grad():
                    dummy_input = torch.zeros(1, 3, 224, 224)
                    if model_name == "ViT_Base":
                        actual_features = self.base_model(dummy_input).last_hidden_state[:, 0]
                    else:
                        actual_features = self.base_model(dummy_input)
                    num_features = actual_features.shape[1]
                    if num_features != reported_features:
                        logging.debug(
                            f"Feature mismatch: reported {reported_features}, actual {num_features}. Using actual.")
            except Exception:
                logging.debug(f"Feature verification failed. Using reported features: {reported_features}")
                num_features = reported_features

            self.classifier = nn.Sequential(
                nn.Linear(num_features, dense_units),
                nn.ReLU(),
                nn.BatchNorm1d(dense_units),
                nn.Dropout(dropout),
                nn.Linear(dense_units, 1),
            )
        except Exception as e:
            logging.error(f"Error initializing CustomModel architecture '{model_name}': {e}", exc_info=True)
            raise

    def forward(self, x):
        if self.model_name == "ViT_Base":
            features = self.base_model(x).last_hidden_state[:, 0]
        else:
            features = self.base_model(x)
        output = self.classifier(features)
        return output


def parse_args():
    parser = argparse.ArgumentParser(description='Image Falsification Detection')
    parser.add_argument('--model', required=True, type=Path, help='Path to PyTorch model state_dict file (.pth)')
    parser.add_argument('--image', required=True, type=Path, help='Path to image file to analyze')
    parser.add_argument('--output', required=True, type=Path, help='Path to save JSON output')
    parser.add_argument('--arch', required=True, type=str, help='Architecture name used during training (e.g., '
                                                                'EfficientNetV2-S)')
    parser.add_argument('--img-height', required=True, type=int, help='Image height the model expects')
    parser.add_argument('--img-width', required=True, type=int, help='Image width the model expects')
    parser.add_argument('--dense-units', required=True, type=int, help='Number of dense units in the classifier head')
    parser.add_argument('--dropout', required=True, type=float, help='Dropout rate used in the classifier head')
    return parser.parse_args()


def load_model(model_path, arch, dense_units, dropout):
    logging.info(f"Attempting to load model state_dict from: {model_path}")
    logging.info(f"Reconstructing architecture: {arch} (Dense: {dense_units}, Dropout: {dropout})")
    try:
        device = torch.device('cuda' if torch.cuda.is_available() else 'cpu')
        logging.info(f"Using device: {device}")

        model = CustomModel(
            model_name=arch,
            dense_units=dense_units,
            dropout=dropout,
            pretrained=False,
            unfreeze_layers=0
        )

        state_dict = torch.load(model_path, map_location=device)
        logging.info(f"State dictionary loaded successfully.")

        model.load_state_dict(state_dict)
        logging.info(f"State dictionary applied to model instance.")

        model.eval()
        model.to(device)
        logging.info(f"Model loaded successfully and set to evaluation mode on {device}.")
        return model, device

    except FileNotFoundError:
        logging.error(f"Model file not found at {model_path}")
        raise
    except Exception as e:
        logging.error(f"Error loading model state_dict from {model_path}: {str(e)}", exc_info=True)
        raise


def preprocess_image(image_path, img_height, img_width):
    logging.debug(f"Preprocessing image: {image_path} to size ({img_height}, {img_width})")
    try:
        img = Image.open(image_path).convert('RGB')

        imagenet_mean = [0.485, 0.456, 0.406]
        imagenet_std = [0.229, 0.224, 0.225]

        preprocess = A.Compose([
            A.Resize(img_height, img_width),
            A.Normalize(mean=imagenet_mean, std=imagenet_std),
            ToTensorV2(),
        ])

        image_np = np.array(img)
        augmented = preprocess(image=image_np)
        img_tensor = augmented['image']

        img_tensor = img_tensor.unsqueeze(0)
        logging.debug(f"Preprocessing complete. Tensor shape: {img_tensor.shape}")
        return img_tensor
    except UnidentifiedImageError:
        logging.error(f"Cannot identify image file (corrupted or wrong format): {image_path}")
        raise
    except FileNotFoundError:
        logging.error(f"Image file not found during preprocessing: {image_path}")
        raise
    except Exception as e:
        logging.error(f"Error preprocessing image {image_path}: {str(e)}", exc_info=True)
        raise


def detect_falsification(model, img_tensor, device):
    logging.debug(f"Running inference on device: {device}")
    try:
        img_tensor = img_tensor.to(device)

        with torch.no_grad():
            output_logits = model(img_tensor)
            probability_falsified = torch.sigmoid(output_logits).item()
            is_falsified = bool(probability_falsified > 0.5)
            confidence = probability_falsified

            result = {
                "isFalsified": is_falsified,
                "confidenceScore": confidence,
                "detectionDetails": {
                    "logit_value": output_logits.item(),
                }
            }
            logging.info(f"Inference complete. Falsified: {is_falsified}, Confidence: {confidence:.4f}")
            return result
    except Exception as e:
        logging.error(f"Error during model inference: {str(e)}", exc_info=True)
        raise


def main():
    args = parse_args()
    output_path = args.output

    try:
        if not args.model.is_file():
            raise FileNotFoundError(f"Model file not found: {args.model}")
        if not args.image.is_file():
            raise FileNotFoundError(f"Image file not found: {args.image}")

        output_path.parent.mkdir(parents=True, exist_ok=True)
        logging.info(f"Output will be saved to: {output_path}")

        model, device = load_model(args.model, args.arch, args.dense_units, args.dropout)
        img_tensor = preprocess_image(args.image, args.img_height, args.img_width)
        result = detect_falsification(model, img_tensor, device)

        with open(output_path, 'w') as f:
            json.dump(result, f, indent=2)

        logging.info(f"Analysis completed successfully. Results saved.")
        return 0

    except Exception as e:
        error_message = f"Error during analysis: {str(e)}"
        logging.error(error_message, exc_info=True)

        try:
            with open(output_path, 'w') as f:
                json.dump({"error": error_message}, f, indent=2)
            logging.info(f"Error details saved to {output_path}")
        except Exception as write_err:
            logging.error(f"Additionally, failed to write error details to {output_path}: {str(write_err)}",
                          exc_info=True)

        return 1


if __name__ == "__main__":
    sys.exit(main())
