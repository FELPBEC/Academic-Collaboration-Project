package co.edu.uptc.Model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class NodoTest {
    @Test 
    void equals_ObjetoOriginal_DeberiaRetornarTrue(){
        Nodo<String> nodo= new Nodo<String>("user");
        assertTrue(nodo.equals(nodo));
    }

    @Test 
    void equals_DiferenteObjetoMismoContenido_DeberiaRetornarTrue(){
        Nodo<String> nodo1= new Nodo<String>("user");
        Nodo<String> nodo2= new Nodo<String>("user");
        assertTrue(nodo1.equals(nodo2));
    }

    @Test 
    void equals_DiferenteObjetoDiferenteContenido_DeberiaRetornarFalse(){
        Nodo<String> nodo1= new Nodo<String>("user");
        Nodo<String> nodo2= new Nodo<String>("user2");
        assertFalse(nodo1.equals(nodo2));
    }

    @Test 
    void equals_objetoNull_DeberiaRetornarFalse(){
        Nodo<String> nodo1= new Nodo<String>("user");
        Nodo<String> nodo2= null;
        assertFalse(nodo1.equals(nodo2));
    }

    @Test 
    void equals_objetoDeDiferenteTipo_DeberiaRetornarFalse(){
        Nodo<String> nodo1= new Nodo<String>("user");
        String nodo2="user";
        assertFalse(nodo1.equals(nodo2));
    }

    @Test 
    void hashCode_DiferenteObjetoMismoContenido_DeberianGenerarHashesIguales(){
        Nodo<String> nodo= new Nodo<String>("user");
        Nodo<String> nodo2= new Nodo<String>("user");
        assertEquals(nodo.hashCode(),nodo2.hashCode());
    }

    @Test 
    void hashCode_DiferenteObjetoDiferenteContenido_DeberianGenerarHashesDiferentes(){
        Nodo<String> nodo= new Nodo<String>("user");
        Nodo<String> nodo2= new Nodo<String>("user1");
        assertNotEquals(nodo.hashCode(),nodo2.hashCode());
    }

    @Test 
    void hashCode_objetoNull_DeberiaRetornar0(){
        Nodo<String> nodo2= new Nodo<String>(null);
        assertEquals(0,nodo2.hashCode());
    }
}
