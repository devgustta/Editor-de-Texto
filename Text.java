import javax.swing.*;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoManager;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.*;

public class Text extends JFrame {
    private JTextArea textArea;
    UndoManager undoManager = new UndoManager();
    JMenuItem undoOption;
    JMenuItem redoOption;
    private JFileChooser fileChooser;

    public Text() throws IOException {
        setTitle("Editor de Texto");
        setSize(900, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JLabel ArchiveName = new JLabel();

        textArea = new JTextArea();
        textArea.setLineWrap(true); // Habilita a quebra de linhas
        textArea.setWrapStyleWord(true); // Quebra as linhas por palavras (não por caracteres)



        JScrollPane scrollPane = new JScrollPane(textArea,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, // Barra vertical sempre visível
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER); // Barra horizontal nunca visivel

        add(scrollPane, BorderLayout.CENTER);
        // Menu Principal da barra de navegaçãp
        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("Arquivo");
        JMenu editMenu = new JMenu("Edit");
        menuBar.add(fileMenu);
        menuBar.add(editMenu);
        setJMenuBar(menuBar);


        // Menu de manipulação( Filhos de "Arquivo")
        JMenuItem openItem = new JMenuItem("Abrir");
        JMenuItem saveItem = new JMenuItem("Salvar");
         undoOption = new JMenuItem("Desfazer");
         redoOption = new JMenuItem("Refazer");
        fileMenu.add(openItem);
        fileMenu.add(saveItem);

        editMenu.add(undoOption);
        editMenu.add(redoOption);

        openItem.addActionListener(e -> openFile());
        saveItem.addActionListener(e -> saveFile());

        // Adiciona o UndoManager ao JTextArea
        textArea.getDocument().addUndoableEditListener(e -> undoManager.addEdit(e.getEdit()));

        // teclas de atalho para o undo e redo
       textArea.addKeyListener(new java.awt.event.KeyAdapter(){
           @Override
           public void keyPressed(KeyEvent e) {

               if (e.isControlDown() && e.getKeyCode() == KeyEvent.VK_Z) {

                   if (undoManager.canUndo()) {
                       undoManager.undo();
                       updateUndoRedoButtons();
                       return;
                   }
                  // undoManager.undo();
               } else if (e.isControlDown() && e.getKeyCode() == KeyEvent.VK_Y) {
                   if (undoManager.canRedo()){
                       undoManager.redo();
                       updateUndoRedoButtons();
                       return;
                   }
               }
                // Ativa meu botão de undo ao escerever algo no meu arquivo aberto
               char letter = e.getKeyChar();

               if(Character.isLetterOrDigit(letter)){
                   undoOption.setEnabled(true);
               }

           }
       });



       // Eventos dos botes undo e redo
       undoOption.addActionListener(e -> {
            try {

                if(undoManager.canUndo()) {
                    undoManager.undo();
                    updateUndoRedoButtons();
                }
            } catch (CannotUndoException ex) {
                ex.printStackTrace();
            }
        });

        redoOption.addActionListener(e -> {
            try {
                if(undoManager.canRedo()){
                    undoManager.redo();
                    updateUndoRedoButtons();
                }
            }catch(CannotUndoException ex){
                ex.printStackTrace();
            }
        });

        // quando o programa é inicilizado pela primeira vez desativa os botões
        updateUndoRedoButtons();
        // Adiciona um listener para atualizar os botões sempre que o texto for alterado
        textArea.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {

            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                updateUndoRedoButtons();

            }

            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                updateUndoRedoButtons();


            }

            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                updateUndoRedoButtons();


            }
        });

       
        setResizable(false); // setando para não redimensionar a janela
        setVisible(true);
    }

    public static void main(String[] args) throws IOException {

        new Text();
    }

    private void updateUndoRedoButtons(){
        undoOption.setEnabled(undoManager.canUndo());
        redoOption.setEnabled(undoManager.canRedo());
    }

    private void saveFile(){
        if (fileChooser == null || fileChooser.getSelectedFile() == null) {
            JOptionPane.showMessageDialog(this, "Nenhum arquivo selecionado para salvar.");
            return;
        }

        if(!textArea.getText().isEmpty()){
            try( BufferedWriter bf = new BufferedWriter(new FileWriter(fileChooser.getSelectedFile()))){
                bf.write(textArea.getText());
                bf.flush();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        }else{
            JOptionPane.showMessageDialog(this, "Não é possível salvar um arquivo vazio");
            return;
        }
    }


    private void openFile(){
        fileChooser = new JFileChooser(); // intanciando a janela de navegação de arquivos
        int option = fileChooser.showOpenDialog(this); // mostrando o gerenciador de arquivo

        if(option == JFileChooser.APPROVE_OPTION){
            File file = fileChooser.getSelectedFile(); // pega o arquivo selecionado

            if(file.getName().endsWith(".TXT") || file.getName().endsWith(".txt")){ // abre o arquivo apenas se terminar com .txt
                try (BufferedReader reader = new BufferedReader(new FileReader(file))){ //  carrega o arquivo no buffer e le linha por linha

                    undoManager.discardAllEdits();// limpa o historico de arquivo anteriores
                    textArea.setText("");
                    textArea.read(reader, null); // escreve o texto na tela
                    textArea.getDocument().addUndoableEditListener(e -> undoManager.addEdit(e.getEdit()));
                    undoOption.setEnabled(false);
                    redoOption.setEnabled(false);

                } catch (IOException e){

                    JOptionPane.showMessageDialog(this, "Erro ao abrir o arquivo: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
                }
            }else{
                JOptionPane.showMessageDialog(this, "Arquvo não suportado");
            }

        }
    }
}