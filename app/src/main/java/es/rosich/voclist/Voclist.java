package es.rosich.voclist;

import java.io.*;
import java.util.*;
import android.util.*;
import android.content.Context;

public class Voclist {

    Vector todas, conseguidas, pendientes, preguntar;
    HashMap<String, Integer> notas;
    Random randomGen = new Random();
    Iterator sacaSiguiente;
    int aprendidas, olvidadas;
    String nombre;
    Context contxt;

    public Voclist(String nombre, InputStream corpus, Context  contexto)
              throws IOException,ClassNotFoundException {

        this.nombre=nombre;
        todas=new Vector();
        conseguidas=new Vector();
        preguntar=new Vector();
        pendientes=new Vector();
        aprendidas=0;
        olvidadas=0;
        contxt=contexto;

        // Cargar corpus
        BufferedReader reader = new BufferedReader(new InputStreamReader(corpus));
        String str;
        while ((str = reader.readLine()) != null) {
             todas.add(str);
        }
        reader.close();

        // Cargar notas anteriores
        try {
            ObjectInputStream fichpuntos=new ObjectInputStream(contxt.openFileInput(nombre + ".pun"));
            notas= (HashMap<String, Integer>) fichpuntos.readObject();
        } catch (IOException e) {
            //BUG: Si se modifica corpus, es necesario borrar datos app. ie, perder puntos
            notas = new HashMap<String, Integer>();
            Iterator it = todas.iterator();
            while(it.hasNext()) {
                notas.put((String)it.next(),1);
            }
        }

        // Separar conseguidas y pendientes
        Iterator it = todas.iterator();
        while(it.hasNext()) {
            String pal=(String)it.next();
            if (notas.get(pal)==2) {
                conseguidas.add(pal);
            } else {
                pendientes.add(pal);
            }
        }

        sacaSiguiente=seleccion();
    }

    public int numsabidas() {
        return(conseguidas.size()+aprendidas);
    }

    public String califica(String palabra, String respuesta) {

        int increm;

        // acierto, 1 punto; fallo -2 puntos
        if (respuesta.equals(palabra.split(",")[0])) increm=+1;
                                                else increm=-2;

        int nota = notas.get(palabra);

        Log.i(palabra,String.valueOf(nota)+"->"+(nota+increm));

        nota+=increm;
        if (nota<0) nota=0;
        if (nota>2) nota=2;

        notas.put(palabra,nota);

        if ((nota==2)&&!conseguidas.contains(palabra)) {
            aprendidas++;
            return("APRENDIDA: "+palabra.split(",")[0]);
        }

        if ((nota==0)&&conseguidas.contains(palabra)) {
            olvidadas++;
            return("OLVIDADA: "+palabra.split(",")[0]);
        }

        if (increm>0) return ("Correcto: " + respuesta);
        else          return ("Mal. Es: "  + palabra.split(",")[0]  );
    }

    public String nuevaPal() {
        if (sacaSiguiente.hasNext()) {
            return (String) sacaSiguiente.next();
        }
        return(null);
    }

    public String pronuncia(String pal) {
        // Palabra a pronunciar. Si hay coma, es "escrito,pronunciar". Ejs.:
        //   table,mesa
        //   ha,ha del verbo haber
        try { return pal.split(",")[1]; } catch(Exception e) {}
        return pal;
    }

    private Iterator seleccion() {
        for (int i=0; i<Math.min(8,pendientes.size()); i++) {
            preguntar.add(pendientes.elementAt(i));
            //Log.i("Añadida pendiente: ",(String)preguntar.lastElement());
        }
        for (int i=0; i<Math.min(2,conseguidas.size()); i++) {
            String otra;
            // al azar entre las sabidas, pero sin repetir
            do {
                otra=(String)conseguidas.elementAt(randomGen.nextInt(conseguidas.size()));
            } while (preguntar.contains(otra));
            preguntar.add(otra);
            Log.i("Añadida ya sabida: ",(String)preguntar.lastElement());
        }
        return preguntar.iterator();
    }

    public String finmsg() throws IOException {
        ObjectOutputStream fichpuntos = new ObjectOutputStream(
                 contxt.openFileOutput(nombre+".pun", Context.MODE_PRIVATE));
        fichpuntos.writeObject(notas);

        String msg="";
        if (aprendidas==1) msg+="Has aprendido "+aprendidas+" palabra";
        if (aprendidas>1)  msg+="Has aprendido "+aprendidas+" palabras";
        if (olvidadas==1)  msg+="Has olvidado " +olvidadas+" palabra";
        if (olvidadas>1)   msg+="Has olvidado " +olvidadas+" palabras";
        msg+="\nSabes "+numsabidas()+" palabras";
        return msg;
    }
}
