package com.darwinruiz.hospital;

import com.darwinruiz.hospital.console.HospitalConsoleApp;
import com.darwinruiz.hospital.utils.EncodingUtils;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

public class HospitalApplication {

    public static void main(String[] args) {

        try {
            System.setProperty("file.encoding", "UTF-8");
            System.setProperty("console.encoding", "UTF-8");
            EncodingUtils.configurarConsola();
        } catch (Exception e) {

        }

        System.out.println("=== SISTEMA DE HOSPITAL ===");



        try {
            EntityManagerFactory emf = Persistence.createEntityManagerFactory("HospitalSystemPU");
            EntityManager em = emf.createEntityManager();

            System.out.println("✓ Conexión a base de datos establecida correctamente");

            em.close();
            emf.close();

            System.out.println("✓ Estructura del proyecto configurada correctamente");
            System.out.println();

            HospitalConsoleApp consoleApp = new HospitalConsoleApp();
            consoleApp.iniciar();

        } catch (Exception e) {
            System.err.println("Error al conectar con la base de datos: " + e.getMessage());
            System.err.println("Verifique la configuración de PostgreSQL en persistence.xml");
            System.err.println("La aplicación no puede continuar sin conexión a la base de datos.");
        }
    }
}
