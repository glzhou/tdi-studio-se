/*
 * XML Type:  ExportCompressedXmlRequest
 * Namespace: http://schemas.microsoft.com/crm/2007/WebServices
 * Java type: com.microsoft.schemas.crm._2007.webservices.ExportCompressedXmlRequest
 *
 * Automatically generated - do not modify.
 */
package com.microsoft.schemas.crm._2007.webservices.impl;
/**
 * An XML ExportCompressedXmlRequest(@http://schemas.microsoft.com/crm/2007/WebServices).
 *
 * This is a complex type.
 */
public class ExportCompressedXmlRequestImpl extends com.microsoft.schemas.crm._2007.webservices.impl.RequestImpl implements com.microsoft.schemas.crm._2007.webservices.ExportCompressedXmlRequest
{
    
    public ExportCompressedXmlRequestImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName PARAMETERXML$0 = 
        new javax.xml.namespace.QName("http://schemas.microsoft.com/crm/2007/WebServices", "ParameterXml");
    private static final javax.xml.namespace.QName EMBEDDEDFILENAME$2 = 
        new javax.xml.namespace.QName("http://schemas.microsoft.com/crm/2007/WebServices", "EmbeddedFileName");
    
    
    /**
     * Gets the "ParameterXml" element
     */
    public java.lang.String getParameterXml()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(PARAMETERXML$0, 0);
            if (target == null)
            {
                return null;
            }
            return target.getStringValue();
        }
    }
    
    /**
     * Gets (as xml) the "ParameterXml" element
     */
    public org.apache.xmlbeans.XmlString xgetParameterXml()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().find_element_user(PARAMETERXML$0, 0);
            return target;
        }
    }
    
    /**
     * Sets the "ParameterXml" element
     */
    public void setParameterXml(java.lang.String parameterXml)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(PARAMETERXML$0, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.SimpleValue)get_store().add_element_user(PARAMETERXML$0);
            }
            target.setStringValue(parameterXml);
        }
    }
    
    /**
     * Sets (as xml) the "ParameterXml" element
     */
    public void xsetParameterXml(org.apache.xmlbeans.XmlString parameterXml)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().find_element_user(PARAMETERXML$0, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.XmlString)get_store().add_element_user(PARAMETERXML$0);
            }
            target.set(parameterXml);
        }
    }
    
    /**
     * Gets the "EmbeddedFileName" element
     */
    public java.lang.String getEmbeddedFileName()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(EMBEDDEDFILENAME$2, 0);
            if (target == null)
            {
                return null;
            }
            return target.getStringValue();
        }
    }
    
    /**
     * Gets (as xml) the "EmbeddedFileName" element
     */
    public org.apache.xmlbeans.XmlString xgetEmbeddedFileName()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().find_element_user(EMBEDDEDFILENAME$2, 0);
            return target;
        }
    }
    
    /**
     * Sets the "EmbeddedFileName" element
     */
    public void setEmbeddedFileName(java.lang.String embeddedFileName)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(EMBEDDEDFILENAME$2, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.SimpleValue)get_store().add_element_user(EMBEDDEDFILENAME$2);
            }
            target.setStringValue(embeddedFileName);
        }
    }
    
    /**
     * Sets (as xml) the "EmbeddedFileName" element
     */
    public void xsetEmbeddedFileName(org.apache.xmlbeans.XmlString embeddedFileName)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().find_element_user(EMBEDDEDFILENAME$2, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.XmlString)get_store().add_element_user(EMBEDDEDFILENAME$2);
            }
            target.set(embeddedFileName);
        }
    }
}