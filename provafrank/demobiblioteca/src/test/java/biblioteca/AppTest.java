package biblioteca;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * Unit test for simple App.
 */
public class AppTest 
{
    /**
     * Rigorous Test :-)
     */
    @Test
    public void shouldAnswerWitpackage com.biblioteca.test;

import com.biblioteca.controller.LibraryController;
import com.biblioteca.model.Book;
import com.biblioteca.model.User;

import java.time.LocalDate;

public class LibraryTest {
    public static void main(String[] args) {
        LibraryController controller = new LibraryController();

        User u = new User();
        u.setName("Vitor");
        controller.saveUser(u);

        Book b = new Book();
        b.setTitle("Java Básico");
        b.setQuantity(3);
        controller.saveBook(b);

        String res = controller.loanBook(u, b, LocalDate.now());
        System.out.println(res); // “Empréstimo realizado.”
    }
}
hTrue()
    {
        assertTrue( true );
    }
}
