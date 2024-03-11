/*
 * Copyright (C) JasonPercus Systems, Inc - All Rights Reserved
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 *
 * Written by JasonPercus, 12/2023
 */
package com.jasonpercus.util;



/**
 * Cette classe permet d'exécuter une application sur le système d'exploitation courant
 * @author JasonPercus
 * @version 1.1
 */
public class App {

    
    
//CONSTRUCTOR
    /**
     * Crée un objet {@link App}
     * @deprecated Ne pas utiliser
     */
    @Deprecated
    private App() {}

    
    
//METHODES PUBLICS STATICS
    /**
     * Exécute une commande sur le système d'exploitation courant
     * @param args Correspond à l'application à exécuter et sa liste de paramètres
     * @return Retourne la réponse de retour d'exécution du programme appelée
     * @throws java.io.IOException Si une erreur survient lors de l'exécution du programme appelé
     */
    public static Result execute(String... args) throws java.io.IOException {
        return App.execute(false, false, false, args);
    }
    
    /**
     * Exécute une commande sur le système d'exploitation courant
     * @param logStd Détermine si les logs de la sortie standard doivent s'afficher au fur et à mesure
     * @param args Correspond à l'application à exécuter et sa liste de paramètres
     * @return Retourne la réponse de retour d'exécution du programme appelée
     * @throws java.io.IOException Si une erreur survient lors de l'exécution du programme appelé
     */
    public static Result execute(boolean logStd, String... args) throws java.io.IOException {
        return App.execute(logStd, true, true, args);
    }
    
    /**
     * Exécute une commande sur le système d'exploitation courant
     * @param logStd Détermine si les logs de la sortie standard doivent s'afficher au fur et à mesure
     * @param logErr Détermine si les logs de la sortie erreur doivent s'afficher au fur et à mesure
     * @param args Correspond à l'application à exécuter et sa liste de paramètres
     * @return Retourne la réponse de retour d'exécution du programme appelée
     * @throws java.io.IOException Si une erreur survient lors de l'exécution du programme appelé
     */
    public static Result execute(boolean logStd, boolean logErr, String... args) throws java.io.IOException {
        return App.execute(logStd, logErr, true, args);
    }
    
    /**
     * Exécute une commande sur le système d'exploitation courant
     * @param logStd Détermine si les logs de la sortie standard doivent s'afficher au fur et à mesure
     * @param logErr Détermine si les logs de la sortie erreur doivent s'afficher au fur et à mesure
     * @param redirectErrOnStd Détermine si les logs de la sortie erreur doivent s'afficher au fur et à mesure sur la sortie standard
     * @param args Correspond à l'application à exécuter et sa liste de paramètres
     * @return Retourne la réponse de retour d'exécution du programme appelée
     * @throws java.io.IOException Si une erreur survient lors de l'exécution du programme appelé
     */
    public static Result execute(boolean logStd, boolean logErr, boolean redirectErrOnStd, String... args) throws java.io.IOException {
        ProcessBuilder builder = new ProcessBuilder(args);
        Process process = builder.start();
        StringBuilder builderInput = new StringBuilder("");
        StringBuilder builderError = new StringBuilder("");
        try (java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.InputStreamReader(process.getInputStream()))) {
            int cpt = 0;
            String line;
            while((line = reader.readLine()) != null){
                if(logStd)
                    System.out.println(line);
                if(cpt > 0) builderInput.append("\n");
                builderInput.append(line);
                cpt++;
            }
        }
        try (java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.InputStreamReader(process.getErrorStream()))) {
            int cpt = 0;
            String line;
            while((line = reader.readLine()) != null){
                if(logErr) {
                    if(redirectErrOnStd)
                        System.out.println(line);
                    else
                        System.err.println(line);
                }
                if(cpt > 0) builderError.append("\n");
                builderError.append(line);
                cpt++;
            }
        }
        return new Result(process.exitValue(), builderInput.toString(), builderError.toString());
    }
    
    
    
//CLASS
    /**
     * Cette classe représente un objet Result qui représente le résultat d'une commande sur le système d'exploitation courant
     * @author JasonPercus
     * @version 1.0
     */
    public static class Result {
        
        
        
    //ATTRIBUTS
        /**
         * Correspond au code de retour de l'exécution
         */
        private final int returnCode;
        
        /**
         * Correspond au texte du flux standard d'entrée
         */
        private final String resultInput;
        
        /**
         * Correspond au texte du flux d'erreur d'entrée
         */
        private final String resultError;

        
        
    //CONSTRUCTOR
        /**
         * Crée un objet {@linkplain Result}
         * @param returnCode Correspond au code de retour de l'exécution
         * @param resultInput Correspond au texte du flux standard d'entrée
         * @param resultError Correspond au texte du flux d'erreur d'entrée
         */
        public Result(int returnCode, String resultInput, String resultError) {
            this.returnCode = returnCode;
            this.resultInput = resultInput;
            this.resultError = resultError;
        }

        
        
    //METHODES PUBLICS
        /**
         * Renvoie le code de retour d'exécution
         * @return Retourne le code de retour d'exécution
         */
        public int getReturnCode() {
            return returnCode;
        }

        /**
         * Renvoie la chaîne de caractères qui représente le flux d'entrée standard
         * @return Retourne la chaîne de caractères qui représente le flux d'entrée standard
         */
        public String getResultInput() {
            return resultInput;
        }

        /**
         * Renvoie la chaîne de caractères qui représente le flux d'entrée erreur
         * @return Retourne la chaîne de caractères qui représente le flux d'entrée erreur
         */
        public String getResultError() {
            return resultError;
        }
        
        
        
    }
    
    
    
}