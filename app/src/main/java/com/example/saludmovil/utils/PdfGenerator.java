package com.example.saludmovil.utils;

import android.content.ContentValues;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.widget.Toast;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;

import com.example.saludmovil.data.MedicamentoRecetado;

public class PdfGenerator {

    public static void generarPdfReceta(Context context,
                                        String folio,
                                        String fecha,
                                        String doctor,
                                        String especialidad,
                                        String cmp,
                                        String paciente,
                                        String dniPaciente,
                                        String diagnostico,
                                        ArrayList<MedicamentoRecetado> medicamentos) {
        PdfDocument document = new PdfDocument();

        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(595, 842, 1).create();
        PdfDocument.Page page = document.startPage(pageInfo);
        Canvas canvas = page.getCanvas();
        Paint paint = new Paint();

        paint.setColor(Color.BLUE);
        paint.setTextSize(24);
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        canvas.drawText("RECETA MÉDICA", 200, 50, paint);

        paint.setColor(Color.BLACK);
        paint.setTextSize(12);
        paint.setTypeface(Typeface.DEFAULT);

        int y = 100;
        canvas.drawText("Folio N°: " + folio, 50, y, paint);
        canvas.drawText("Fecha: " + fecha, 400, y, paint);

        y += 30;
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        canvas.drawText("PACIENTE:", 50, y, paint);
        paint.setTypeface(Typeface.DEFAULT);
        canvas.drawText(paciente + " (DNI: " + dniPaciente + ")", 150, y, paint);

        y += 30;
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        canvas.drawText("DOCTOR:", 50, y, paint);
        paint.setTypeface(Typeface.DEFAULT);
        canvas.drawText(doctor + " (" + especialidad + ") - CMP: " + cmp, 150, y, paint);

        y += 40;
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        canvas.drawText("DIAGNÓSTICO:", 50, y, paint);
        y += 20;
        paint.setTypeface(Typeface.DEFAULT);
        canvas.drawText(diagnostico, 50, y, paint);

        y += 30;
        paint.setColor(Color.LTGRAY);
        canvas.drawLine(50, y, 545, y, paint);
        paint.setColor(Color.BLACK);
        y += 30;
        paint.setTextSize(14);
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        canvas.drawText("MEDICAMENTOS (Rp/)", 50, y, paint);

        paint.setTextSize(12);
        paint.setTypeface(Typeface.DEFAULT);
        y += 10;

        for (MedicamentoRecetado med : medicamentos) {
            y += 30;
            paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
            canvas.drawText("• " + med.getNombre(), 60, y, paint);
            canvas.drawText(med.getCantidad(), 450, y, paint);
            y += 15;
            paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.ITALIC));
            paint.setColor(Color.DKGRAY);
            canvas.drawText("  Indicaciones: " + med.getIndicaciones(), 60, y, paint);
            paint.setColor(Color.BLACK);
        }
        canvas.drawText("Generado por SaludMóvil App", 200, 800, paint);

        document.finishPage(page);
        guardarPdfEnDescargas(context, document, "Receta_" + folio + ".pdf");
    }

    private static void guardarPdfEnDescargas(Context context, PdfDocument document, String nombreArchivo) {
        try {
            ContentValues values = new ContentValues();
            values.put(MediaStore.MediaColumns.DISPLAY_NAME, nombreArchivo);
            values.put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf");
            values.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);

            Uri uri = context.getContentResolver().insert(MediaStore.Files.getContentUri("external"), values);

            if (uri != null) {
                OutputStream outputStream = context.getContentResolver().openOutputStream(uri);
                document.writeTo(outputStream);
                if (outputStream != null) outputStream.close();

                Toast.makeText(context, "PDF guardado en Descargas: " + nombreArchivo, Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(context, "Error al crear el archivo PDF", Toast.LENGTH_SHORT).show();
            }

            document.close();

        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(context, "Error al generar PDF: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}