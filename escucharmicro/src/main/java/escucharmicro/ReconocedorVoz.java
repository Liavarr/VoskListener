package escucharmicro;

import org.vosk.Model;
import org.vosk.Recognizer;
import org.vosk.LibVosk;
import org.vosk.LogLevel;

import javax.sound.sampled.*;
import java.io.IOException;

public class ReconocedorVoz {

    private Recognizer recognizer; // Objeto para reconocer voz
    private Model model; // Modelo de reconocimiento de voz
    private String lastCommand = ""; // Último comando reconocido
    private volatile boolean running = true; // Variable para controlar la ejecución

    public ReconocedorVoz(Model model) throws IOException {
        this.model = model;
        this.recognizer = new Recognizer(model, 16000); // Inicializa el reconocedor con el modelo y la frecuencia
    }

    public void procesarAudio() throws IOException, LineUnavailableException {
        // Configura el micrófono
        AudioFormat format = new AudioFormat(16000, 16, 1, true, false);
        TargetDataLine microphone;
        DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);

        if (!AudioSystem.isLineSupported(info)) {
            System.err.println("Micrófono no soportado");
            return; // Si el micrófono no es soportado, salir
        }

        microphone = (TargetDataLine) AudioSystem.getLine(info);
        microphone.open(format);
        microphone.start();

        // Buffer para leer el audio
        byte[] buffer = new byte[4096];
        int bytesRead;

        // Procesa el audio en tiempo real
        System.out.println("Comenzando a procesar audio...");
        try {
            while (running) { // Usa la variable running para controlar el ciclo
                bytesRead = microphone.read(buffer, 0, buffer.length);
                if (bytesRead > 0) {
                    if (recognizer.acceptWaveForm(buffer, bytesRead)) {
                        String resultado = recognizer.getResult(); // Obtiene el resultado del reconocimiento

                        // Solo imprime el resultado si es diferente al último comando
                        if (!resultado.equals(lastCommand) && !resultado.isEmpty()) {
                            System.out.println("Resultado: " + resultado);
                            lastCommand = resultado; // Actualiza el último comando reconocido
                        }
                    }
                }
            }
        } finally {
            microphone.stop(); // Detiene el micrófono
            microphone.close(); // Cierra el micrófono
        }
    }

    // Método para detener el reconocimiento
    public void detener() {
        running = false; // Cambia la variable para salir del ciclo
    }

    // Método para cerrar el reconocedor
    public void cerrar() {
        recognizer.close(); // Cierra el reconocedor
    }

    public static void main(String[] args) throws LineUnavailableException {
        // Inicializa la biblioteca de Vosk
        LibVosk.setLogLevel(LogLevel.INFO);

        // Especifica la ruta del modelo
        String modeloRuta = "C:\\Program Files\\Java\\VoskModels\\vosk-model-es-0.42";

        try (Model model = new Model(modeloRuta)) { // Intenta crear un modelo a partir de la ruta
            ReconocedorVoz transcripcion = new ReconocedorVoz(model); // Crea una instancia del reconocedor de voz

            // Crea un hilo para procesar audio
            Thread audioThread = new Thread(() -> {
                try {
                    transcripcion.procesarAudio(); // Procesa el audio
                } catch (IOException | LineUnavailableException e) {
                    e.printStackTrace(); // Imprime el error en caso de que ocurra
                }
            });
            audioThread.start(); // Inicia el hilo de procesamiento de audio

            // Espera a que el usuario presione Enter para detener el programa
            System.out.println("Presiona Enter para detener el programa...");
            System.in.read(); // Lee la entrada del usuario
            transcripcion.detener(); // Detiene el procesamiento de audio
            try {
				audioThread.join();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} // Espera a que el hilo termine
            transcripcion.cerrar(); // Cierra el reconocedor

        } catch (IOException e) {
            e.printStackTrace(); // Imprime el error en caso de que ocurra
        }
    }
}
