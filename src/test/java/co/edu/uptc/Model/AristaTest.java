package co.edu.uptc.Model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

class AristaTest {
    @Test 
    void getExtremoOpuesto_NodosEnlazados_DeberiaRetornarNodoB(){
        Nodo<String> A= new Nodo<String>("A");
        Nodo<String> B= new Nodo<String>("B");
        Relation relacion= new Relation();
        Arista<String> enlace= new Arista<>(A, B, relacion, 0);
        assertEquals(enlace.getExtremoOpuesto(A), B);
        assertEquals(enlace.getExtremoOpuesto(B), A);
    } 
    @Test 
    void getExtremoOpuesto_NodosSinAristas_DeberiaRetornarNull(){
        Nodo<String> A= new Nodo<String>("A");
        Nodo<String> B= new Nodo<String>("B");
        Nodo<String> C= new Nodo<String>("C");
        Relation relacion= new Relation();
        Arista<String> enlace= new Arista<>(A, B, relacion, 0);
        assertNull(enlace.getExtremoOpuesto(C));
    } 
    @Test 
    void getExtremoOpuesto_NodoNull_DeberiaRetornarNull(){
        Nodo<String> A= new Nodo<String>("A");
        Nodo<String> B= new Nodo<String>("B");
        Nodo<String> C= new Nodo<String>(null);
        Relation relacion= new Relation();
        Arista<String> enlace= new Arista<>(A, B, relacion, 0);
        assertNull(enlace.getExtremoOpuesto(C));
    } 
    @Test 
    void getExtremoOpuesto_NodoNullEnlazado_DeberiaRetornarNull(){
        Nodo<String> A= new Nodo<String>("A");
        Nodo<String> B= new Nodo<String>("B");
        Relation relacion= new Relation();
        Arista<String> enlace= new Arista<>(B, A, relacion, 0);
        assertNull(enlace.getExtremoOpuesto(null));
    } 
    

}
