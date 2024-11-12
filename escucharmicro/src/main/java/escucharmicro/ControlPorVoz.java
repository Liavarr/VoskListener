package escucharmicro;

import org.vosk.Model;
import org.vosk.Recognizer;
import org.vosk.LibVosk;
import org.vosk.LogLevel;

import javax.sound.sampled.*;
import java.io.IOException;

public class ControlPorVoz {

    private Recognizer recognizer; // Objeto para reconocimiento de voz
    private Model model; // Modelo de reconocimiento de voz

    public ControlPorVoz(Model model) throws IOException {
        this.model = model;
        this.recognizer = new Recognizer(model, 16000); // Inicializa el reconocedor con el modelo y frecuencia
    }

    public void procesarComandos() throws IOException, LineUnavailableException {
        // Configura el micrófono
        AudioFormat format = new AudioFormat(16000, 16, 1, true, false);
        TargetDataLine microphone;
        DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);

        if (!AudioSystem.isLineSupported(info)) {
            System.err.println("Micrófono no soportado");
            return; // Salir si el micrófono no es soportado
        }

        microphone = (TargetDataLine) AudioSystem.getLine(info);
        microphone.open(format);
        microphone.start(); // Comienza a capturar audio

        // Buffer para leer el audio
        byte[] buffer = new byte[4096];
        int bytesRead;

        // Procesa el audio en tiempo real
        try {
            System.out.println("Esperando comandos de voz...");
            while (true) {
                bytesRead = microphone.read(buffer, 0, buffer.length);
                if (bytesRead > 0) {
                    if (recognizer.acceptWaveForm(buffer, bytesRead)) {
                        String resultado = recognizer.getResult(); // Obtiene el resultado del reconocimiento
                        ejecutarComando(resultado, microphone); // Ejecuta el comando reconocido
                    }
                }
            }
        } finally {
            microphone.stop(); // Detiene el micrófono
            microphone.close(); // Cierra el micrófono
        }
    }

    private void ejecutarComando(String resultado, TargetDataLine microphone) {
        // Aquí se interpretan los comandos reconocidos
        if (resultado.contains("abrir")) {
            System.out.println("Comando recibido: Abrir");
            // Lógica para abrir un archivo o aplicación
        } else if (resultado.contains("cerrar")) {
            System.out.println("Comando recibido: Cerrar");
            microphone.stop(); // Detiene el micrófono
            microphone.close(); // Cierra el micrófono
            // Lógica para cerrar un archivo o aplicación
        } else if (resultado.contains("guardar")) {
            System.out.println("Comando recibido: Guardar");
            // Lógica para guardar un archivo
        } else {
            System.out.println("Comando no reconocido: " + resultado); // Mensaje si el comando no es reconocido
        }
    }

    public static void main(String[] args) throws LineUnavailableException {
        // Inicializa la biblioteca de Vosk
        LibVosk.setLogLevel(LogLevel.INFO);

        // Especifica la ruta del modelo
        String modeloRuta = "C:\\Program Files\\Java\\VoskModels\\vosk-model-small-es-0.42"; // Cambia esta ruta a tu modelo

        try (Model model = new Model(modeloRuta)) { // Intenta crear un modelo a partir de la ruta
            ControlPorVoz control = new ControlPorVoz(model); // Crea una instancia del controlador de voz
            control.procesarComandos(); // Comienza a procesar los comandos
        } catch (IOException e) {
            e.printStackTrace(); // Imprime el error en caso de que ocurra
        }
    }
}

