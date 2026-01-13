package dados;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class GerenciadorDados {

    public static boolean salvarObjeto(Object obj, String nomeArquivo) {
        File arq = new File(nomeArquivo);
        try (FileOutputStream gravador = new FileOutputStream(arq, true)) {
            ObjectOutputStream conversor;
            if (arq.length() == 0) {
                conversor = new ObjectOutputStream(gravador);
            } else {
                conversor = new AppendableObjectOutputStream(gravador);
            }
            conversor.writeObject(obj);
            conversor.close();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
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
                return lista;
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        return lista;
    }

    public static boolean reescreverArquivo(List<?> lista, String nomeArquivo) {
        File arq = new File(nomeArquivo);
        try (FileOutputStream gravador = new FileOutputStream(arq, false);
             ObjectOutputStream conversor = new ObjectOutputStream(gravador)) {
            for (Object obj : lista) {
                conversor.writeObject(obj);
            }
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
}