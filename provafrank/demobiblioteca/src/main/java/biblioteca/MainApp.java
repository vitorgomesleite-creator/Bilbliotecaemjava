package com.biblioteca;

import com.biblioteca.util.HibernateUtil;
import com.biblioteca.view.MainFrame;

import javax.swing.*;

public class MainApp {

```
public static void main(String[] args) {

    // Inicializa o Hibernate
    try {
        HibernateUtil.getSessionFactory();
    } catch (Throwable ex) {
        System.err.println("Erro ao iniciar o Hibernate:");
        ex.printStackTrace();
        return;
    }

    // Fecha o Hibernate ao sair
    Runtime.getRuntime().addShutdownHook(new Thread(() -> {
        HibernateUtil.shutdown();
    }));

    // Inicia GUI
    SwingUtilities.invokeLater(() -> {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}
        //oi 
        MainFrame frame = new MainFrame();
        frame.setVisible(true);
    });
}
```

}
