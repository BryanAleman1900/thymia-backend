package com.project.demo.logic.entity.wellness;

import com.project.demo.logic.entity.user.User;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
public class WellnessAdviceGenerator {

    public record Advice(String title, String content, String category) {}

    public Advice generate(User user, String text, String label, Double score) {
        if (text == null || text.isBlank()) return null;

        String safeLabel = label != null ? label.toUpperCase(Locale.ROOT) : "UNKNOWN";
        double s = score != null ? score : 0.0;

        String lc = text.toLowerCase(Locale.ROOT);


        boolean ansiedad = containsAny(lc, "ansiedad","ansioso","angustia","nervioso","panic");
        boolean tristeza = containsAny(lc, "triste","deprim","desanimado","vacío");
        boolean enojo   = containsAny(lc, "enojo","ira","frustrado","molesto","rabia");
        boolean insomnio= containsAny(lc, "insomnio","dormir","desvelo","no duermo");

        switch (safeLabel) {
            case "NEGATIVE" -> {
                if (ansiedad && s >= 0.55) {
                    return new Advice(
                            "Anclaje y respiración (2 min)",
                            """
                            Nota ansiedad. Prueba el ejercicio 4-6 por 2 minutos:
                            • Inhala contando 4.
                            • Exhala contando 6.
                            • Repite 8–10 ciclos.
                            Suma una técnica 5-4-3-2-1 (menciona 5 cosas que ves, 4 que sientes, 3 que oyes, 2 que hueles, 1 que saboreas).
                            Si persiste, agenda un espacio breve para moverte o estirarte 3 minutos.
                            """,
                            "ansiedad"
                    );
                }
                if (tristeza && s >= 0.55) {
                    return new Advice(
                            "Micro-activación (5 min)",
                            """
                            Cuando hay bajón de ánimo, elige una tarea micro (≤5 min): tender la cama,
                            un vaso de agua, abrir la ventana y respirar aire fresco, o escribir 3 cosas neutrales que hiciste hoy.
                            Marca 1 acción ahora mismo y regístrala.
                            """,
                            "estado_de_animo"
                    );
                }
                if (enojo) {
                    return new Advice(
                            "Descarga física segura (3–5 min)",
                            """
                            El enojo pide movimiento. Opciones: apretar una pelota antiestrés 60s, 20 sentadillas,
                            o escribir sin filtro 2 minutos y luego romper/archivar la hoja. Cierra con 4 respiraciones lentas.
                            """,
                            "regulacion_emocional"
                    );
                }
                if (insomnio) {
                    return new Advice(
                            "Higiene de sueño esta noche",
                            """
                            • Apaga pantallas 30 min antes de dormir.
                            • Nota 3 pensamientos en una 'lista de pendientes para mañana'.
                            • Realiza 6 respiraciones 4-6.
                            Si no duermes en 20 min, levántate y lee algo ligero con luz tenue.
                            """,
                            "suenio"
                    );
                }

                return new Advice(
                        "Pausa consciente (2 min)",
                        """
                        Pon un temporizador de 2 minutos.
                        • Inhala por la nariz 4, exhala 6, repetido 10 veces.
                        • Al final, nombra en voz baja: “ahora mismo estoy a salvo”.
                        """,
                        "regulacion_emocional"
                );
            }
            case "POSITIVE" -> {
                return new Advice(
                        "Savoring rápido (2 min)",
                        """
                        Ancla este buen momento: describe 3 detalles específicos que te agradaron hoy
                        (olor, color, frase, gesto). Relee en 24h para reforzar memoria emocional positiva.
                        """,
                        "gratitud_savoring"
                );
            }
            case "NEUTRAL", "UNKNOWN" -> {
                return new Advice(
                        "Chequeo corporal (1 min)",
                        """
                        Escanea de cabeza a pies, detecta tensión y suelta los hombros.
                        Bebe agua y estira cuello 30 segundos por lado. Pequeños resets, gran efecto.
                        """,
                        "autocuidado_basico"
                );
            }
            default -> {
                return new Advice(
                        "Chequeo corporal (1 min)",
                        "Escanea tensión, suelta hombros. Bebe agua y estira cuello 30s por lado.",
                        "autocuidado_basico"
                );
        }
    }
    }
    private boolean containsAny(String text, String... needles) {
        for (String n : needles) if (text.contains(n)) return true;
        return false;
    }
}


