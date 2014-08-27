package br.com.pense.devtools.view;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.apache.commons.lang3.StringUtils;

import br.com.pense.devtools.model.ParameterDef;
import br.com.pense.devtools.model.ParameterList;

public class ParametersPanel extends JPanel {
    private static final long serialVersionUID = 1L;
    
    private static Map<Class<?>, Class<? extends JComponent>> MAP_COMPONENT_PER_PARAM_TYPE = new HashMap<Class<?>, Class<? extends JComponent>>();
    
    static {
        MAP_COMPONENT_PER_PARAM_TYPE.put(String.class, JTextField.class);
        MAP_COMPONENT_PER_PARAM_TYPE.put(Boolean.class, JCheckBox.class);
    }
    
    private ParameterList _parameters;
    private Map<String, JComponent> cacheComponentPerParamName = new HashMap<String, JComponent>();
    
    private int labelWidth;
    private int labelHeight;

    public ParametersPanel(ParameterList parameters, int labelWidth, int labelHeight) {
        super(new BorderLayout());
        
        this.labelWidth = labelWidth;
        this.labelHeight = labelHeight;
        
        this._parameters = parameters;
        init();
    }
    
    private void init() {
        if (_parameters != null && !_parameters.isEmpty()) {
            JPanel pnlItens = new JPanel(new GridLayout(_parameters.size(), 1));
                        
            for (ParameterDef parameterDef : _parameters) {
                JComponent cmp = getComponentForParameter(parameterDef);
                cacheComponentPerParamName.put(parameterDef.getKey(), cmp);
                pnlItens.add(getLayoutComponent(parameterDef.getTitle(), cmp));
            }
            add(pnlItens, BorderLayout.NORTH);
        } else {
            add(new JLabel("  Sem Parametros "), BorderLayout.NORTH);
        }
    }
    
    public void setParametersValues(Map<String, Object> values) {
        
        Iterator<String> keys = values.keySet().iterator();
        while (keys.hasNext()) {
            String k = keys.next();
            Object value = values.get(k);

            JComponent cmp = cacheComponentPerParamName.get(k);
            if (cmp != null) {
                if (cmp instanceof JTextField) {
                    if (value != null && StringUtils.isNotEmpty(value.toString())) {
                        ((JTextField)cmp).setText(value.toString());
                    } else {
                        ((JTextField)cmp).setText("");
                    }
                } else if (cmp instanceof JCheckBox) {
                    Boolean vBoolean;
                    if (value instanceof Boolean) {
                        vBoolean = (Boolean)value;
                    } else {
                        vBoolean = Boolean.valueOf(value.toString());
                    }
                    
                    ((JCheckBox)cmp).setSelected(vBoolean);
                } else {
                    throw new RuntimeException("Componente do tipo [" + cmp.getClass().getName() + "] não previsto.");
                }
            }
        }
        
    }
    
    public Map<String, Object> getParameterValues() {
        Map<String, Object> ret = new HashMap<String, Object>();

        if (!cacheComponentPerParamName.isEmpty()) {
            Iterator<String> keys = cacheComponentPerParamName.keySet().iterator();
            while (keys.hasNext()) {
                String k = keys.next();
                JComponent cmp = cacheComponentPerParamName.get(k);
                if (cmp != null) {
                    if (cmp instanceof JTextField) {
                        ret.put(k, ((JTextField)cmp).getText());
                    } else if (cmp instanceof JCheckBox) {
                        ret.put(k, ((JCheckBox)cmp).isSelected());
                    } else {
                        throw new RuntimeException("Componente do tipo [" + cmp.getClass().getName() + "] não previsto.");
                    }
                }
            }
        }
        
        return ret;
    }
    
    private JPanel getLayoutComponent(String name, JComponent cmp) {
        if (cmp instanceof JCheckBox) {
            JLabel lblText = new JLabel(" " + name, JLabel.LEFT);
            lblText.setPreferredSize(new Dimension(this.labelWidth, this.labelHeight));
            
            cmp.setAlignmentX(JCheckBox.RIGHT_ALIGNMENT);
            
            JPanel pnlLayoutCheck = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
            pnlLayoutCheck.setPreferredSize(new Dimension(this.labelWidth, this.labelHeight));
            pnlLayoutCheck.add(cmp);
            
            JPanel pnlLayout = new JPanel(new BorderLayout(0, 0));
            pnlLayout.add(lblText, BorderLayout.CENTER);
            pnlLayout.add(pnlLayoutCheck, BorderLayout.WEST);
            return pnlLayout;
            
        } else {
            JLabel lblText = new JLabel(name+": ", JLabel.RIGHT);
            lblText.setPreferredSize(new Dimension(this.labelWidth, this.labelHeight));

            JPanel pnlLayout = new JPanel(new BorderLayout(0, 0));
            pnlLayout.add(lblText, BorderLayout.WEST);
            pnlLayout.add(cmp, BorderLayout.CENTER);
            return pnlLayout;
        }
    }
    
    private JComponent getComponentForParameter(ParameterDef parameterDef) {
        Class<? extends JComponent> cmpClass = MAP_COMPONENT_PER_PARAM_TYPE.get(parameterDef.getType());
        if (cmpClass == null) {
            System.out.println("Não achou componente para classe [" + parameterDef.getType() + "] usando textfield padrao");
            cmpClass = JTextField.class;
        }
        
        try {
            return cmpClass.newInstance();
        } catch (InstantiationException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
    
    
}
