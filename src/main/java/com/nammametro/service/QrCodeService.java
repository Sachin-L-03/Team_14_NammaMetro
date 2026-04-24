package com.nammametro.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;

/**
 * Generates QR code images using the ZXing library.
 * Used to display QR codes on ticket detail pages.
 *
 * SRP: This class has one responsibility — generating QR code images.
 */
@Service
public class QrCodeService {

    private static final int QR_WIDTH = 250;
    private static final int QR_HEIGHT = 250;

    /**
     * Generates a Base64-encoded PNG QR code image from the given content.
     *
     * @param content the data to encode in the QR code
     * @return Base64-encoded PNG string (ready for <img src="data:image/png;base64,...">)
     */
    public String generateQrCodeBase64(String content) {
        try {
            QRCodeWriter writer = new QRCodeWriter();
            BitMatrix matrix = writer.encode(content, BarcodeFormat.QR_CODE, QR_WIDTH, QR_HEIGHT);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(matrix, "PNG", baos);

            return Base64.getEncoder().encodeToString(baos.toByteArray());
        } catch (WriterException | IOException e) {
            throw new RuntimeException("Failed to generate QR code", e);
        }
    }

    /**
     * Creates the JSON content for a ticket QR code.
     */
    public String createTicketQrContent(Long ticketId, Long passengerId, Long scheduleId) {
        return "{\"ticketId\":" + ticketId
                + ",\"passengerId\":" + passengerId
                + ",\"scheduleId\":" + scheduleId + "}";
    }
}
