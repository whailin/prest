/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package common.gui.actions;

import categorizer.core.Categorizer;
import common.DataContext;
import definitions.application.ApplicationProperties;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 *
 * @author Gürhan
 */
public class StoreCategorizerAdapter implements ActionListener{
    
    private Categorizer categorizer;

    public StoreCategorizerAdapter(Categorizer categorizer) {
        this.categorizer = categorizer;
    }

    public void actionPerformed(ActionEvent e) {
        /*
        try {
            DataContext context = new DataContext();
            categorizer.setTitle("categorizerai");
            context.add("categorizerai", categorizer.store());
            context.writeToFile(ApplicationProperties.get("repositorylocation") + 
                    "\\" + packageExplorer.getProjectDirectory().getName() + "\\categorizer" + Calendar.getInstance().getTimeInMillis()+".xml");
            JOptionPane.showMessageDialog(null, "Categorizer stored successfully!", "Store Result", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            Logger.getLogger(PrestGuiView.class.getName()).log(Level.SEVERE, null, ex);
        }
         */ 
    }
    
    public Categorizer getCategorizer() {
        return categorizer;
    }

    public void setCategorizer(Categorizer categorizer) {
        this.categorizer = categorizer;
    }

}