package com.pcd.report.service;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.pcd.report.model.Report;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Service
@Slf4j
public class PdfGenerationService {

    private static final Font TITLE_FONT = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD);
    private static final Font HEADER_FONT = new Font(Font.FontFamily.HELVETICA, 14, Font.BOLD);
    private static final Font SUBHEADER_FONT = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD);
    private static final Font NORMAL_FONT = new Font(Font.FontFamily.HELVETICA, 10, Font.NORMAL);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public byte[] generatePdf(Report report) {
        log.info("Generating PDF for report: {}", report.getId());

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4);
            PdfWriter.getInstance(document, baos);

            document.open();

            // Add title
            Paragraph title = new Paragraph(report.getTitle(), TITLE_FONT);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);
            document.add(Chunk.NEWLINE);

            // Add case information
            document.add(new Paragraph("Case Information", HEADER_FONT));
            document.add(new Paragraph("Case Number: " + report.getCaseNumber(), NORMAL_FONT));
            document.add(new Paragraph("Status: " + report.getStatus(), NORMAL_FONT));
            document.add(new Paragraph("Report Type: " + report.getReportType(), NORMAL_FONT));
            document.add(new Paragraph("Created: " + (report.getCreatedAt() != null ? report.getCreatedAt().format(DATE_FORMATTER) : "N/A"), NORMAL_FONT));
            document.add(Chunk.NEWLINE);

            // Add description if available
            if (report.getDescription() != null && !report.getDescription().isEmpty()) {
                document.add(new Paragraph("Description", HEADER_FONT));
                document.add(new Paragraph(report.getDescription(), NORMAL_FONT));
                document.add(Chunk.NEWLINE);
            }

            // Add analyses
            if (report.getAnalyses() != null && !report.getAnalyses().isEmpty()) {
                document.add(new Paragraph("Analysis Results", HEADER_FONT));

                for (int i = 0; i < report.getAnalyses().size(); i++) {
                    Map<String, Object> analysis = report.getAnalyses().get(i);
                    document.add(new Paragraph("Analysis #" + (i + 1), SUBHEADER_FONT));

                    // Add a summary of the analysis result
                    if (analysis.containsKey("isFalsified") && analysis.containsKey("confidenceScore")) {
                        String resultText = "Result: " + (Boolean.TRUE.equals(analysis.get("isFalsified")) ?
                                "Image appears to be falsified" : "No falsification detected");
                        document.add(new Paragraph(resultText, SUBHEADER_FONT));

                        String confidenceText = "Confidence Score: " + analysis.get("confidenceScore") + "%";
                        document.add(new Paragraph(confidenceText, NORMAL_FONT));
                        document.add(Chunk.NEWLINE);
                    }

                    PdfPTable table = new PdfPTable(2);
                    table.setWidthPercentage(100);

                    // Add key details to the table
                    for (Map.Entry<String, Object> entry : analysis.entrySet()) {
                        if (entry.getValue() != null && !entry.getKey().equals("detailedResults")) {
                            PdfPCell keyCell = new PdfPCell(new Phrase(entry.getKey(), NORMAL_FONT));
                            PdfPCell valueCell = new PdfPCell(new Phrase(entry.getValue().toString(), NORMAL_FONT));

                            table.addCell(keyCell);
                            table.addCell(valueCell);
                        }
                    }

                    document.add(table);

                    // Add detailed results if available
                    if (analysis.containsKey("detailedResults") && analysis.get("detailedResults") != null) {
                        document.add(new Paragraph("Detailed Results:", SUBHEADER_FONT));

                        @SuppressWarnings("unchecked")
                        Map<String, Object> detailedResults = (Map<String, Object>) analysis.get("detailedResults");

                        if (!detailedResults.isEmpty()) {
                            PdfPTable detailsTable = new PdfPTable(2);
                            detailsTable.setWidthPercentage(100);

                            for (Map.Entry<String, Object> detail : detailedResults.entrySet()) {
                                if (detail.getValue() != null) {
                                    PdfPCell keyCell = new PdfPCell(new Phrase(detail.getKey(), NORMAL_FONT));
                                    PdfPCell valueCell = new PdfPCell(new Phrase(detail.getValue().toString(), NORMAL_FONT));

                                    detailsTable.addCell(keyCell);
                                    detailsTable.addCell(valueCell);
                                }
                            }

                            document.add(detailsTable);
                        }
                    }

                    document.add(Chunk.NEWLINE);
                }
            }

            // Add verdict if available
            if (report.getVerdict() != null && !report.getVerdict().isEmpty()) {
                document.add(new Paragraph("Verdict", HEADER_FONT));
                document.add(new Paragraph(report.getVerdict(), NORMAL_FONT));
                document.add(Chunk.NEWLINE);
            }

            // Add judicial notes if available
            if (report.getJudicialNotes() != null && !report.getJudicialNotes().isEmpty()) {
                document.add(new Paragraph("Judicial Notes", HEADER_FONT));
                document.add(new Paragraph(report.getJudicialNotes(), NORMAL_FONT));
                document.add(Chunk.NEWLINE);
            }

            // Add footer
            document.add(new Paragraph("Generated by: " + (report.getGeneratedBy() != null ? report.getGeneratedBy() : "System"), NORMAL_FONT));
            document.add(new Paragraph("Report ID: " + report.getId(), NORMAL_FONT));

            document.close();

            log.info("PDF generated successfully for report: {}", report.getId());
            return baos.toByteArray();

        } catch (Exception e) {
            log.error("Error generating PDF for report: {}", report.getId(), e);
            throw new RuntimeException("Failed to generate PDF report", e);
        }
    }
}
