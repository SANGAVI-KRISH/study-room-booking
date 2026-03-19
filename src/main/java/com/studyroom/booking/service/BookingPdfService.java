package com.studyroom.booking.service;

import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import com.studyroom.booking.model.Booking;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class BookingPdfService {

    public byte[] generateBookingHistoryPdf(List<Booking> bookings) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        Document document = new Document(PageSize.A4.rotate(), 20, 20, 20, 20);

        try {
            PdfWriter.getInstance(document, out);
            document.open();

            Font titleFont = new Font(Font.HELVETICA, 18, Font.BOLD);
            Font headerFont = new Font(Font.HELVETICA, 12, Font.BOLD, Color.WHITE);
            Font bodyFont = new Font(Font.HELVETICA, 11, Font.NORMAL);

            Paragraph title = new Paragraph("Booking History Report", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(15);
            document.add(title);

            PdfPTable table = new PdfPTable(7);
            table.setWidthPercentage(100);
            table.setSpacingBefore(10f);

            table.setWidths(new float[]{2.8f, 2.2f, 2.2f, 2.2f, 2.5f, 2.2f, 3.0f});

            addHeaderCell(table, "Room Name", headerFont);
            addHeaderCell(table, "Booking Date", headerFont);
            addHeaderCell(table, "Start Time", headerFont);
            addHeaderCell(table, "End Time", headerFont);
            addHeaderCell(table, "Status", headerFont);
            addHeaderCell(table, "Purpose", headerFont);
            addHeaderCell(table, "Booked By", headerFont);

            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
            DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("hh:mm a");

            if (bookings == null || bookings.isEmpty()) {
                PdfPCell emptyCell = new PdfPCell(new Phrase("No booking history available", bodyFont));
                emptyCell.setColspan(7);
                emptyCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                emptyCell.setPadding(10f);
                table.addCell(emptyCell);
            } else {
                for (Booking booking : bookings) {
                    table.addCell(createBodyCell(
                            booking.getRoom() != null ? booking.getRoom().getBlockName() : "-", bodyFont));

                    table.addCell(createBodyCell(
                            booking.getStartAt() != null ? booking.getStartAt().toLocalDate().format(dateFormatter) : "-", bodyFont));

                    table.addCell(createBodyCell(
                            booking.getStartAt() != null ? booking.getStartAt().toLocalTime().format(timeFormatter) : "-", bodyFont));

                    table.addCell(createBodyCell(
                            booking.getEndAt() != null ? booking.getEndAt().toLocalTime().format(timeFormatter) : "-", bodyFont));

                    table.addCell(createBodyCell(
                            booking.getStatus() != null ? booking.getStatus().name() : "-", bodyFont));

                    table.addCell(createBodyCell(
                            booking.getPurpose() != null ? booking.getPurpose() : "-", bodyFont));

                    table.addCell(createBodyCell(
                            booking.getUser() != null ? booking.getUser().getName() : "-", bodyFont));
                }
            }

            document.add(table);
            document.close();

        } catch (Exception e) {
            throw new RuntimeException("Failed to generate booking history PDF", e);
        }

        return out.toByteArray();
    }

    private void addHeaderCell(PdfPTable table, String text, Font font) {
        PdfPCell header = new PdfPCell(new Phrase(text, font));
        header.setBackgroundColor(new Color(52, 73, 94));
        header.setHorizontalAlignment(Element.ALIGN_CENTER);
        header.setVerticalAlignment(Element.ALIGN_MIDDLE);
        header.setPadding(8f);
        table.addCell(header);
    }

    private PdfPCell createBodyCell(String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setPadding(6f);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        return cell;
    }
}