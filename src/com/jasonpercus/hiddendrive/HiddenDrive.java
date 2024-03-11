/*
 * Copyright (C) JasonPercus Systems, Inc - All Rights Reserved
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 *
 * Written by JasonPercus, 03/2024
 */
package com.jasonpercus.hiddendrive;



import com.jasonpercus.util.App;
import com.jasonpercus.util.App.Result;



/**
 * Cette classe représente une fenêtre Swing pour sélectionner les lecteurs à cacher.
 * @author JasonPercus
 * @version 1.0
 */
public class HiddenDrive extends javax.swing.JFrame {

    
    
//CONSTANTES    
    /**
     * Correspond au nom du programme Windows permettant d'exécuter des commandes batch
     */
    private final static String CMD                    = "cmd.exe";
    
    /**
     * Paramètre pour la commande cmd.exe, permettant de définir une commande à exécuter.
     */
    private final static String CMD_COMMAND            = "/C";
    
    /**
     * Commande cmd pour obtenir la valeur du registre (regedit) qui détermine quels lecteurs sont cachés.
     */
    private final static String REG_GET                = "reg query %s /v NoDrives";
    
    /**
     * Commande cmd pour définir la valeur du registre (regedit) qui détermine quels lecteurs seront cachés.
     */
    private final static String REG_SET                = "reg add %s /t REG_DWORD /v NoDrives /D %d /F";
    
    /**
     * Clé du registre (regedit) à manipuler.
     */
    private final static String KEY                    = "HKEY_LOCAL_MACHINE\\Software\\Microsoft\\Windows\\CurrentVersion\\Policies\\Explorer";
    
    /**
     * Nom du groupe de capture dans le motif regex.
     */
    private final static String REGEX_GET_GROUP_NAME   = "value";
    
    /**
     * Modèle de motif regex pour extraire la valeur du registre.
     */
    private final static String REGEX_PATTERN          = String.format("^\\s*NoDrives\\s*REG_DWORD\\s*0x(?<%s>\\w*)$", REGEX_GET_GROUP_NAME);
    
    /**
     * Modèle regex compilé pour extraire la valeur du registre.
     */
    private final static java.util.regex.Pattern REGEX = java.util.regex.Pattern.compile(REGEX_PATTERN);
    
    
    
//CONSTRUCTOR
    /**
     * Constructeur de la classe HiddenDrive.
     */
    public HiddenDrive() {
        initComponents();
        list.setSelectedIndices(getIndicesToSelect());
    }
    
    
    
//MAIN
    /**
     * Méthode principale pour exécuter l'application.
     * @param args Les arguments éventuels de la ligne de commande.
     */
    public static void main(String args[]) {
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Windows".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(HiddenDrive.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>
        
        //Si le programme n'est pas exécuté en mode administrateur
        if(!checkAdmin()){
            javax.swing.JOptionPane.showMessageDialog(null, "Veuillez relancer le programme en mode administrateur !", "Démarrage...", javax.swing.JOptionPane.ERROR_MESSAGE);
            System.exit(2);
        }
        
        java.awt.EventQueue.invokeLater(() -> {
            new HiddenDrive().setVisible(true);
        });
    }
    
    
    
//METHODE PRIVATE STATIC
    /**
     * Renvoie si oui ou non le programme est exécuté en mode administrateur ou pas
     * @return Retourne true si c'est le cas, sinon false
     */
    private static boolean checkAdmin(){
        java.io.File testAdmin = new java.io.File("C:\\Windows\\System32\\TestFile");
        boolean result = false;
        if(!testAdmin.exists()){
            try {
                boolean created = testAdmin.createNewFile();
                boolean deleted = testAdmin.delete();
                result = created && deleted;
            } catch (java.io.IOException ex) {}
        }
        return result;
    }
    
    
    
//METHODES PRIVATES
    /**
     * Récupère les indices à sélectionner de la JList {@link #list} en fonction de la valeur obtenue dans le registre (regedit).
     * @return Un tableau d'indices à sélectionner.
     */
    private int[] getIndicesToSelect() {
        int regeditValue = getValue();
        java.util.List<Boolean> mbs = new java.util.ArrayList<>(26);
        java.util.List<Integer> tab = new java.util.ArrayList<>(26);
        
        for(int i = 0; i < 26; i++)
            tab.add(0, (int) Math.pow(2, i));
        
        int div;
        for(int i : tab) {
            div          = regeditValue / i;
            regeditValue = regeditValue % i;
            mbs.add(0, div >= 1);
        }
        
        java.util.List<Integer> toSelect = new java.util.ArrayList<>();
        for(int i = 0; i < mbs.size(); i++) {
            if(mbs.get(i))
                toSelect.add(i);
        }
        
        int[] array = new int[toSelect.size()];
        for(int i=0;i<array.length;i++)
            array[i] = toSelect.get(i);
        return array;
    }
    
    /**
     * Calcule la valeur de registre (regedit) à partir des indices choisis de la JList {@link #list}.
     * @return La valeur qui sera inscrite dans le registre (regedit).
     */
    private int getValueSelected() {
        int[] indices = list.getSelectedIndices();
        if(indices == null) 
            return 0;
        int add = 0;
        for(int i : indices)
            add += (int) Math.pow(2, i);
        return add;
    }
    
    /**
     * Récupère la valeur enregistrée dans le registre (regedit).
     * @return La valeur récupérée ou -1 si une erreur survient.
     */
    private int getValue() {
        try {
            Result result = App.execute(CMD, CMD_COMMAND, String.format(REG_GET, KEY));
            if(result.getReturnCode() != 0)
                return -1;
            for(String line : result.getResultInput().split("\n")) {
                java.util.regex.Matcher matcher = REGEX.matcher(line);
                if(matcher.find())
                    //parse la chaîne de caractère en entier hexadécimal
                    return Integer.parseInt(matcher.group(REGEX_GET_GROUP_NAME), 16);
            }
            return 0;
        } catch (java.io.IOException ex) {
            return -1;
        }
    }

    /**
     * Définit la valeur dans le registre (regedit) en fonction des indices sélectionnés de la JList {@link #list}.
     * @return 0 si l'opération réussit, -1 sinon.
     */
    private int setValue() {
        try {
            Result result = App.execute(CMD, CMD_COMMAND, String.format(REG_SET, KEY, getValueSelected()));
            if(result.getReturnCode() != 0)
                return -1;
            return 0;
        } catch (java.io.IOException ex) {
            return -1;
        }
    }
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        label = new javax.swing.JLabel();
        validateButton = new javax.swing.JButton();
        cancelButton = new javax.swing.JButton();
        scroll = new javax.swing.JScrollPane();
        list = new javax.swing.JList<>();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("HiddenDrive");
        setIconImage(new javax.swing.ImageIcon(getClass().getResource("/assets/icon.png")).getImage());
        setResizable(false);

        label.setText("Sélectionnez les lecteurs qui doivent être cachés:");

        validateButton.setText("Valider");
        validateButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                validateButtonActionPerformed(evt);
            }
        });

        cancelButton.setText("Annuler");
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelButtonActionPerformed(evt);
            }
        });

        list.setModel(new javax.swing.AbstractListModel<String>() {
            String[] strings = { "A:\\", "B:\\", "C:\\", "D:\\", "E:\\", "F:\\", "G:\\", "H:\\", "I:\\", "J:\\", "K:\\", "L:\\", "M:\\", "N:\\", "O:\\", "P:\\", "Q:\\", "R:\\", "S:\\", "T:\\", "U:\\", "V:\\", "W:\\", "X:\\", "Y:\\", "Z:\\" };
                public int getSize() { return strings.length; }
                public String getElementAt(int i) { return strings[i]; }
            });
            scroll.setViewportView(list);

            javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
            getContentPane().setLayout(layout);
            layout.setHorizontalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                    .addContainerGap()
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                            .addComponent(cancelButton)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(validateButton))
                        .addGroup(layout.createSequentialGroup()
                            .addComponent(label)
                            .addGap(0, 0, Short.MAX_VALUE))
                        .addComponent(scroll))
                    .addContainerGap())
            );

            layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {cancelButton, validateButton});

            layout.setVerticalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(label)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(scroll, javax.swing.GroupLayout.DEFAULT_SIZE, 219, Short.MAX_VALUE)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(validateButton)
                        .addComponent(cancelButton))
                    .addContainerGap())
            );

            pack();
            setLocationRelativeTo(null);
        }// </editor-fold>//GEN-END:initComponents

    /**
     * Méthode appelée lorsqu'on appuie sur le bouton de validation.
     * @param evt L'événement déclencheur.
     */
    private void validateButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_validateButtonActionPerformed
        if(setValue() == 0) {
            javax.swing.JOptionPane.showMessageDialog(this, "L'opération a réussi !\nVous devez redémarrer le PC pour que les changements prennent effet...", "Succès", javax.swing.JOptionPane.INFORMATION_MESSAGE);
            System.exit(0);
        } else {
            javax.swing.JOptionPane.showMessageDialog(this, "L'opération a échoué !", "Echec", javax.swing.JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_validateButtonActionPerformed

    /**
     * Méthode appelée lorsqu'on appuie sur le bouton d'annulation.
     * @param evt L'événement déclencheur.
     */
    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelButtonActionPerformed
        System.exit(0);
    }//GEN-LAST:event_cancelButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton cancelButton;
    private javax.swing.JLabel label;
    private javax.swing.JList<String> list;
    private javax.swing.JScrollPane scroll;
    private javax.swing.JButton validateButton;
    // End of variables declaration//GEN-END:variables



}