package de.camundahometask;

public class Invoice {
    String id;
    String bpmn20Xml;

    public Invoice() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getBpmn20Xml() {
        return bpmn20Xml;
    }

    public void setBpmn20Xml(String bpmn20Xml) {
        this.bpmn20Xml = bpmn20Xml;
    }
}

