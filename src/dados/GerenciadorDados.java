package dados;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class GerenciadorDados {

    public static void salvarObjeto(Object obj, String nomeArquivo) throws IOException {
        File arq = new File(nomeArquivo);

        FileOutputStream gravador = new FileOutputStream(arq, true);
        ObjectOutputStream conversor;

        if (arq.length() == 0) {
            conversor = new ObjectOutputStream(gravador);
        } else {
            conversor = new AppendableObjectOutputStream(gravador);
        }

        conversor.writeObject(obj);
        conversor.close();
        gravador.close();
    }

    public static ArrayList<Object> carregarObjetos(String nomeArquivo) {
        ArrayList<Object> lista = new ArrayList<>();
        File arq = new File(nomeArquivo);

        if (arq.exists() && arq.isFile() && arq.length() > 0) {
            try (FileInputStream leitor = new FileInputStream(arq);
                 ObjectInputStream conversor = new ObjectInputStream(leitor)) {

                while (true) {
                    Object obj = conversor.readObject();
                    lista.add(obj);
                }
            } catch (EOFException e) {
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        return lista;
    }

    public static void reescreverArquivo(List<?> lista, String nomeArquivo) throws IOException {
        File arq = new File(nomeArquivo);
        FileOutputStream gravador = new FileOutputStream(arq, false);
        ObjectOutputStream conversor = new ObjectOutputStream(gravador);

        for (Object obj : lista) {
            conversor.writeObject(obj);
        }
        conversor.close();
        gravador.close();
    }
}