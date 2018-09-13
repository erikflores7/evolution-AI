import javax.swing.*;

public class Main {

    static Game g;

    public static void main(String[] args) {
        JFrame frm = new JFrame();
        frm.setTitle("Evolution");
        g = new Game();
        frm.setContentPane(g);
        //frm.setSize(300, 700);
        frm.setSize(800, 800);
        frm.setResizable(false);
        frm.setVisible(true);
        frm.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    static Game getGame(){
        return g;
    }

}
