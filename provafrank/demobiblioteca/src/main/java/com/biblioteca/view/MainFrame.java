package com.biblioteca.view;

import com.biblioteca.controller.LibraryController;
import com.biblioteca.model.Book;
import com.biblioteca.model.Loan;
import com.biblioteca.model.User;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.MaskFormatter;
import java.awt.*;
import java.awt.event.*;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class MainFrame extends JFrame {
    private final LibraryController controller = new LibraryController();
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // Tables
    private final DefaultTableModel bookTableModel = new DefaultTableModel(new String[]{"ID","Título","Autor","ISBN","Data Pub.","Qtd"},0) {
        @Override public boolean isCellEditable(int row, int col) { return false; }
    };
    private final DefaultTableModel userTableModel = new DefaultTableModel(new String[]{"ID","Nome","Sexo","Celular","Email"},0) {
        @Override public boolean isCellEditable(int row, int col) { return false; }
    };
    private final DefaultTableModel loanTableModel = new DefaultTableModel(new String[]{"ID","Usuário","Livro","Empréstimo","Prev.Devol.","Devolução"},0) {
        @Override public boolean isCellEditable(int row, int col) { return false; }
    };
    
    // Referências para os combos de empréstimo
    private JComboBox<User> cbUsers;
    private JComboBox<Book> cbBooks;

    public MainFrame() {
        setTitle("Sistema de Gestão de Biblioteca - MVCR");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900,600);
        setLocationRelativeTo(null);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Livros", createBooksPanel());
        tabs.addTab("Usuários", createUsersPanel());
        final JPanel loansPanel = createLoansPanel();
        tabs.addTab("Empréstimos", loansPanel);
        tabs.addTab("Listagens / Atrasados", createListingPanel());
        
        // Atualizar combos quando mudar para aba de empréstimos
        tabs.addChangeListener(e -> {
            if (tabs.getSelectedIndex() == 2) { // Índice da aba Empréstimos
                refreshAll();
            }
        });

        add(tabs);
        refreshAll();
    }

    private JPanel createBooksPanel() {
        JPanel p = new JPanel(new BorderLayout(8,8));
        p.setBorder(new EmptyBorder(8,8,8,8));

        final JTable table = new JTable(bookTableModel);
        JScrollPane sp = new JScrollPane(table);
        p.add(sp, BorderLayout.CENTER);

        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(4,4,4,4);
        c.anchor = GridBagConstraints.WEST;

        final JTextField tfTitle = new JTextField(20);
        final JTextField tfTheme = new JTextField(15);
        final JTextField tfAuthor = new JTextField(15);
        final JTextField tfIsbn = new JTextField(12);
        final JFormattedTextField tfPubDate[] = new JFormattedTextField[1];
        try {
            tfPubDate[0] = new JFormattedTextField(new MaskFormatter("##/##/####"));
            tfPubDate[0].setColumns(8);
        } catch (ParseException e) { tfPubDate[0] = new JFormattedTextField(); tfPubDate[0].setColumns(8); }
        final JSpinner spQuantity = new JSpinner(new SpinnerNumberModel(1,0,1000,1));

        final JButton btnAdd = new JButton("Salvar/Atualizar");
        final JButton btnDelete = new JButton("Excluir");
        JButton btnClear = new JButton("Limpar");

        c.gridx=0;c.gridy=0; form.add(new JLabel("Título:"),c);
        c.gridx=1; form.add(tfTitle,c);
        c.gridx=0;c.gridy=1; form.add(new JLabel("Tema:"),c);
        c.gridx=1; form.add(tfTheme,c);
        c.gridx=0;c.gridy=2; form.add(new JLabel("Autor:"),c);
        c.gridx=1; form.add(tfAuthor,c);
        c.gridx=0;c.gridy=3; form.add(new JLabel("ISBN:"),c);
        c.gridx=1; form.add(tfIsbn,c);
        c.gridx=0;c.gridy=4; form.add(new JLabel("Data Pub. (dd/MM/yyyy):"),c);
        c.gridx=1; form.add(tfPubDate[0],c);
        c.gridx=0;c.gridy=5; form.add(new JLabel("Quantidade:"),c);
        c.gridx=1; form.add(spQuantity,c);

        c.gridx=0;c.gridy=6; form.add(btnAdd,c);
        c.gridx=1; form.add(btnDelete,c);
        c.gridx=2; form.add(btnClear,c);

        p.add(form, BorderLayout.SOUTH);

        // Selecionar linha -> preencher campos
        table.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                int r = table.getSelectedRow();
                if (r >= 0) {
                    Long id = (Long) table.getValueAt(r,0);
                    Book b = controller.findBook(id);
                    if (b!=null) {
                        tfTitle.setText(b.getTitle());
                        tfTheme.setText(b.getTheme());
                        tfAuthor.setText(b.getAuthor());
                        tfIsbn.setText(b.getIsbn());
                        tfPubDate[0].setText(b.getPublicationDate());
                        spQuantity.setValue(b.getQuantity());
                    }
                }
            }
        });

        btnAdd.addActionListener(ev -> {
            String title = tfTitle.getText().trim();
            if (title.isEmpty()) { JOptionPane.showMessageDialog(this,"Título obrigatório."); return; }
            String theme = tfTheme.getText().trim();
            String author = tfAuthor.getText().trim();
            String isbn = tfIsbn.getText().trim();
            String pub = tfPubDate[0].getText().trim();
            int q = (Integer) spQuantity.getValue();

            // if row selected update existing, else create new
            int r = table.getSelectedRow();
            Book book;
            if (r>=0) {
                Long id = (Long) table.getValueAt(r,0);
                book = controller.findBook(id);
                if (book == null) { JOptionPane.showMessageDialog(this,"Livro não encontrado."); return; }
            } else {
                book = new Book();
            }
            book.setTitle(title); book.setTheme(theme); book.setAuthor(author);
            book.setIsbn(isbn); book.setPublicationDate(pub); book.setQuantity(q);
            controller.saveBook(book);
            refreshBooks();
            JOptionPane.showMessageDialog(this,"Livro salvo.");
        });

        btnDelete.addActionListener(ev -> {
            int rsel = table.getSelectedRow();
            if (rsel < 0) { JOptionPane.showMessageDialog(this,"Selecione um livro."); return; }
            Long id = (Long) table.getValueAt(rsel,0);
            Book b = controller.findBook(id);
            int ok = JOptionPane.showConfirmDialog(this,"Confirmar exclusão do livro '"+b.getTitle()+"'?");
            if (ok==JOptionPane.YES_OPTION) {
                controller.deleteBook(b);
                refreshBooks();
            }
        });

        btnClear.addActionListener(ev -> {
            table.clearSelection();
            tfTitle.setText(""); tfTheme.setText(""); tfAuthor.setText(""); tfIsbn.setText(""); tfPubDate[0].setText(""); spQuantity.setValue(1);
        });

        return p;
    }

    private JPanel createUsersPanel() {
        JPanel p = new JPanel(new BorderLayout(8,8));
        p.setBorder(new EmptyBorder(8,8,8,8));
        JTable table = new JTable(userTableModel);
        p.add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints(); c.insets = new Insets(4,4,4,4); c.anchor = GridBagConstraints.WEST;

        final JTextField tfName = new JTextField(18);
        final JComboBox<String> cbSex = new JComboBox<>(new String[]{"Masculino","Feminino","Outro"});
        final JFormattedTextField tfPhone[];
        tfPhone = new JFormattedTextField[1];
        try { tfPhone[0] = new JFormattedTextField(new MaskFormatter("(##) #####-####")); tfPhone[0].setColumns(10); }
        catch (ParseException e) { tfPhone[0] = new JFormattedTextField(); tfPhone[0].setColumns(10); }
        final JTextField tfEmail = new JTextField(18);
        // Simple email verifier
        tfEmail.setInputVerifier(new InputVerifier() {
            @Override
            public boolean verify(JComponent input) {
                String s = ((JTextField)input).getText();
                return s.contains("@") && s.contains(".");
            }
        });

        final JButton btnSave = new JButton("Salvar/Atualizar");
        final JButton btnDel = new JButton("Excluir");
        final JButton btnClear = new JButton("Limpar");

        c.gridx=0;c.gridy=0; form.add(new JLabel("Nome:"),c);
        c.gridx=1; form.add(tfName,c);
        c.gridx=0;c.gridy=1; form.add(new JLabel("Sexo:"),c);
        c.gridx=1; form.add(cbSex,c);
        c.gridx=0;c.gridy=2; form.add(new JLabel("Celular:"),c);
        c.gridx=1; form.add(tfPhone[0],c);
        c.gridx=0;c.gridy=3; form.add(new JLabel("E-mail:"),c);
        c.gridx=1; form.add(tfEmail,c);

        c.gridx=0;c.gridy=4; form.add(btnSave,c);
        c.gridx=1; form.add(btnDel,c);
        c.gridx=2; form.add(btnClear,c);

        p.add(form, BorderLayout.SOUTH);

        table.addMouseListener(new MouseAdapter(){
            public void mouseClicked(MouseEvent e){
                int r = table.getSelectedRow();
                if (r>=0) {
                    Long id = (Long) table.getValueAt(r,0);
                    User u = controller.findUser(id);
                    tfName.setText(u.getName());
                    cbSex.setSelectedItem(u.getSex());
                    tfPhone[0].setText(u.getPhone());
                    tfEmail.setText(u.getEmail());
                }
            }
        });

        btnSave.addActionListener(ev -> {
            String name = tfName.getText().trim();
            if (name.isEmpty()) { JOptionPane.showMessageDialog(this,"Nome obrigatório."); return; }
            String sex = (String) cbSex.getSelectedItem();
            String phone = tfPhone[0].getText().trim();
            String email = tfEmail.getText().trim();
            if (!email.contains("@") || !email.contains(".")) {
                JOptionPane.showMessageDialog(this,"E-mail inválido."); return;
            }
            int r = table.getSelectedRow();
            User u;
            if (r >= 0) {
                Long id = (Long) table.getValueAt(r,0);
                u = controller.findUser(id);
            } else u = new User();
            u.setName(name); u.setSex(sex); u.setPhone(phone); u.setEmail(email);
            controller.saveUser(u);
            refreshUsers();
        });

        btnDel.addActionListener(ev -> {
            int r = table.getSelectedRow();
            if (r<0) { JOptionPane.showMessageDialog(this,"Selecione um usuário."); return; }
            Long id = (Long) table.getValueAt(r,0);
            User u = controller.findUser(id);
            int ok = JOptionPane.showConfirmDialog(this,"Excluir usuário "+u.getName()+"?");
            if (ok==JOptionPane.YES_OPTION) {
                controller.deleteUser(u);
                refreshUsers();
            }
        });

        btnClear.addActionListener(ev -> {
            table.clearSelection();
            tfName.setText(""); tfPhone[0].setText(""); tfEmail.setText("");
        });

        return p;
    }

    private JPanel createLoansPanel() {
        JPanel p = new JPanel(new BorderLayout(8,8)); p.setBorder(new EmptyBorder(8,8,8,8));

        final JTable table = new JTable(loanTableModel);
        p.add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel south = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints(); c.insets = new Insets(4,4,4,4); c.anchor = GridBagConstraints.WEST;

        cbUsers = new JComboBox<>();
        cbBooks = new JComboBox<>();
        final JFormattedTextField tfLoanDate[] = new JFormattedTextField[1];
        try { tfLoanDate[0] = new JFormattedTextField(new MaskFormatter("##/##/####")); tfLoanDate[0].setColumns(8); tfLoanDate[0].setText(LocalDate.now().format(formatter)); }
        catch (ParseException e) { tfLoanDate[0] = new JFormattedTextField(); tfLoanDate[0].setColumns(8); tfLoanDate[0].setText(LocalDate.now().format(formatter)); }

        JButton btnLoan = new JButton("Fazer Empréstimo");
        JButton btnReturn = new JButton("Registrar Devolução");

        c.gridx=0;c.gridy=0; south.add(new JLabel("Usuário:"),c);
        c.gridx=1; south.add(cbUsers,c);
        c.gridx=0;c.gridy=1; south.add(new JLabel("Livro:"),c);
        c.gridx=1; south.add(cbBooks,c);
        c.gridx=0;c.gridy=2; south.add(new JLabel("Data Empréstimo:"),c);
        c.gridx=1; south.add(tfLoanDate[0],c);
        c.gridx=0;c.gridy=3; south.add(btnLoan,c);
        c.gridx=1; south.add(btnReturn,c);

        p.add(south, BorderLayout.SOUTH);

        // popula combos
        final Runnable[] repopulateCombos = new Runnable[1];
        repopulateCombos[0] = () -> {
            cbUsers.removeAllItems();
            for (User u : controller.listUsers()) cbUsers.addItem(u);
            cbBooks.removeAllItems();
            for (Book b : controller.listBooks()) cbBooks.addItem(b);
        };
        repopulateCombos[0].run();

        btnLoan.addActionListener(ev -> {
            User u = (User) cbUsers.getSelectedItem();
            Book b = (Book) cbBooks.getSelectedItem();
            if (u==null || b==null) { JOptionPane.showMessageDialog(this,"Selecione usuário e livro."); return; }
            try {
                LocalDate loanDate = LocalDate.parse(tfLoanDate[0].getText(), formatter);
                String res = controller.loanBook(u,b,loanDate);
                JOptionPane.showMessageDialog(this,res);
                refreshAll();
                repopulateCombos[0].run();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this,"Data inválida. Use dd/MM/yyyy");
            }
        });

        btnReturn.addActionListener(ev -> {
            int r = table.getSelectedRow();
            if (r<0) { JOptionPane.showMessageDialog(this,"Selecione um empréstimo (na tabela)."); return; }
            Long id = (Long) loanTableModel.getValueAt(r,0);
            Loan loan = controller.listLoans().stream().filter(l -> l.getId().equals(id)).findFirst().orElse(null);
            if (loan==null) { JOptionPane.showMessageDialog(this,"Empréstimo não encontrado."); return; }
            String sdate = JOptionPane.showInputDialog(this,"Data de devolução (dd/MM/yyyy)", LocalDate.now().format(formatter));
            try {
                LocalDate ret = LocalDate.parse(sdate, formatter);
                String res = controller.returnBook(loan, ret);
                JOptionPane.showMessageDialog(this, res);
                refreshAll();
                repopulateCombos[0].run();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this,"Data inválida.");
            }
        });

        return p;
    }

    private JPanel createListingPanel() {
        JPanel p = new JPanel(new BorderLayout(8,8));
        p.setBorder(new EmptyBorder(8,8,8,8));

        JTextArea ta = new JTextArea();
        ta.setEditable(false);
        p.add(new JScrollPane(ta), BorderLayout.CENTER);

        JPanel top = new JPanel();
        JButton btnListAvailable = new JButton("Listar livros disponíveis");
        JButton btnOverdue = new JButton("Listar atrasados hoje");
        top.add(btnListAvailable); top.add(btnOverdue);
        p.add(top, BorderLayout.NORTH);

        btnListAvailable.addActionListener(ev -> {
            StringBuilder sb = new StringBuilder();
            List<Book> books = controller.listBooks();
            for (Book b : books) {
                if (b.getQuantity() > 0)
                    sb.append(String.format("ID:%d - %s - Autor: %s - Qtd:%d%n", b.getId(), b.getTitle(), b.getAuthor(), b.getQuantity()));
            }
            ta.setText(sb.length()==0 ? "Nenhum livro disponível." : sb.toString());
        });

        btnOverdue.addActionListener(ev -> {
            List<Loan> overdue = controller.listOverdue(LocalDate.now());
            StringBuilder sb = new StringBuilder();
            for (Loan l : overdue) {
                long days = java.time.temporal.ChronoUnit.DAYS.between(l.getExpectedReturnDate(), LocalDate.now());
                sb.append(String.format("Empréstimo ID:%d - Usuário:%s - Livro:%s - Prev: %s - Dias atraso: %d%n",
                        l.getId(), l.getUser().getName(), l.getBook().getTitle(), l.getExpectedReturnDate().format(formatter), days));
            }
            ta.setText(sb.length()==0 ? "Nenhum atraso." : sb.toString());
        });

        return p;
    }

    private void refreshBooks() {
        bookTableModel.setRowCount(0);
        for (Book b : controller.listBooks()) {
            bookTableModel.addRow(new Object[]{b.getId(), b.getTitle(), b.getAuthor(), b.getIsbn(), b.getPublicationDate(), b.getQuantity()});
        }
    }
    private void refreshUsers() {
        userTableModel.setRowCount(0);
        for (User u : controller.listUsers()) {
            userTableModel.addRow(new Object[]{u.getId(), u.getName(), u.getSex(), u.getPhone(), u.getEmail()});
        }
    }
    private void refreshLoans() {
        loanTableModel.setRowCount(0);
        for (Loan l : controller.listLoans()) {
            loanTableModel.addRow(new Object[]{
                    l.getId(),
                    l.getUser().getName(),
                    l.getBook().getTitle(),
                    l.getLoanDate() != null ? l.getLoanDate().format(formatter) : "",
                    l.getExpectedReturnDate() != null ? l.getExpectedReturnDate().format(formatter) : "",
                    l.getReturnDate() != null ? l.getReturnDate().format(formatter) : ""
            });
        }
    }
    private void refreshAll() {
        refreshBooks();
        refreshUsers();
        refreshLoans();
        updateLoansCombos();
    }
    
    private void updateLoansCombos() {
        if (cbUsers != null && cbBooks != null) {
            cbUsers.removeAllItems();
            for (User u : controller.listUsers()) cbUsers.addItem(u);
            cbBooks.removeAllItems();
            for (Book b : controller.listBooks()) cbBooks.addItem(b);
        }
    }
}
