// ============================================================================
//
// Copyright (C) 2006-2016 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.designer.core.generic.model;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.avro.Schema;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.gef.commands.CommandStack;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.ui.PlatformUI;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.commons.runtime.model.components.IComponentConstants;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.api.service.ComponentService;
import org.talend.core.model.metadata.IMetadataTable;
import org.talend.core.model.metadata.MetadataToolHelper;
import org.talend.core.model.metadata.builder.connection.MetadataTable;
import org.talend.core.model.process.EParameterFieldType;
import org.talend.core.model.process.ElementParameterParser;
import org.talend.core.model.process.IElement;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.model.process.IProcess;
import org.talend.daikon.NamedThing;
import org.talend.daikon.properties.PresentationItem;
import org.talend.daikon.properties.Property;
import org.talend.daikon.properties.presentation.Form;
import org.talend.daikon.properties.presentation.Widget;
import org.talend.daikon.properties.presentation.Widget.WidgetType;
import org.talend.designer.core.generic.constants.IElementParameterEventProperties;
import org.talend.designer.core.generic.constants.IGenericConstants;
import org.talend.designer.core.generic.utils.ComponentsUtils;
import org.talend.designer.core.generic.utils.SchemaUtils;
import org.talend.designer.core.model.components.ElementParameter;
import org.talend.designer.core.ui.editor.cmd.ChangeMetadataCommand;
import org.talend.designer.core.ui.editor.nodes.Node;

/**
 * created by ycbai on 2015年9月24日 Detailled comment
 *
 */
public class GenericElementParameter extends ElementParameter {

    private ComponentProperties rootProperties;

    private Form form;

    private Widget widget;

    private ComponentService componentService;

    private List<?> possibleValues;

    private boolean supportContext;

    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);

    private boolean isFirstCall;

    private boolean drivedByForm;

    public GenericElementParameter(IElement element, ComponentProperties rootProperties, Form form, Widget widget,
            ComponentService componentService) {
        super(element);
        this.rootProperties = rootProperties;
        this.form = form;
        this.widget = widget;
        this.componentService = componentService;
        isFirstCall = true;

        NamedThing widgetProperty = widget.getContent();
        setName(widgetProperty.getName());
        setDisplayName(widgetProperty.getDisplayName());
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        this.pcs.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        this.pcs.removePropertyChangeListener(listener);
    }

    @Override
    public void setValue(Object o) {
        super.setValue(o);
        if (!isFirstCall || widget.getContent() instanceof ComponentProperties) {
            updateProperty(o);
            boolean calledValidate = callValidate();
            if (calledValidate) {
                fireValidateStatusEvent();
            }
            boolean calledAfter = callAfter();
            if (calledAfter) {
                fireValueChangedEvent();
            }
        }
        isFirstCall = false;
    }

    private void updateProperty(Object newValue) {
        if (getSubProperties() == null) {
            return;
        }

        NamedThing widgetProperty = widget.getContent();
        if (widgetProperty instanceof Property) {
            Property se = (Property) widgetProperty;
            Object oldValue = se.getValue();
            if (newValue != null && !newValue.equals(oldValue)) {
                se = (Property) getSubProperties().getProperty(se.getName());
                if (isDrivedByForm()) {
                    form.setValue(se.getName(), newValue);
                } else {
                    if (newValue instanceof List && WidgetType.TABLE.equals(widget.getWidgetType())) {
                        setTableValues(se, (List<Map<String, Object>>) newValue);
                    } else {
                        se.setValue(newValue);
                    }
                }
                fireConnectionPropertyChangedEvent(newValue);
            }
        } else if (widgetProperty instanceof PresentationItem) {
            PresentationItem pi = (PresentationItem) widgetProperty;
            Form formtoShow = pi.getFormtoShow();
            if (formtoShow != null) {
                fireShowDialogEvent(getSubProperties().getForm(formtoShow.getName()));
            }
        }
    }

    private void setTableValues(Property prop, List<Map<String, Object>> value) {
        List<Map<String, String>> newMap = ElementParameterParser.createTableValues(value, this);
        prop.setValue(newMap);
    }

    private boolean hasPropertyChangeListener() {
        return pcs.getPropertyChangeListeners().length != 0;
    }

    private void fireValidateStatusEvent() {
        if (hasPropertyChangeListener()) {
            this.pcs.firePropertyChange(IElementParameterEventProperties.EVENT_VALIDATE_RESULT_UPDATE, null,
                    getSubProperties().getValidationResult());
        }
    }

    private void fireValueChangedEvent() {
        if (hasPropertyChangeListener()) {
            List<Form> forms = getSubProperties().getForms();
            for (Form f : forms) {
                if (f.isRefreshUI()) {
                    this.pcs.firePropertyChange(IElementParameterEventProperties.EVENT_PROPERTY_VALUE_CHANGED, null, null);
                    return;
                }
            }
        }
    }

    private void fireConnectionPropertyChangedEvent(Object newPropertyName) {
        if (IGenericConstants.NAME_PROPERTY.equalsIgnoreCase(getName()) && hasPropertyChangeListener()) {
            this.pcs.firePropertyChange(IElementParameterEventProperties.EVENT_PROPERTY_NAME_CHANGED, null, newPropertyName);
        }
    }

    private void fireShowDialogEvent(Form formToDisplay) {
        if (hasPropertyChangeListener()) {
            this.pcs.firePropertyChange(IElementParameterEventProperties.EVENT_SHOW_DIALOG, null, formToDisplay);
        }
    }

    public boolean callBeforePresent() {
        if (widget.isCallBeforePresent() && hasPropertyChangeListener()) {
            return new ComponentServiceCaller(widget.getContent().getDisplayName(), widget.isLongRunning()) {

                @Override
                protected void doWork() throws Throwable {
                    componentService.beforePropertyPresent(getParameterName(), getSubProperties());
                }
            }.call();
        }
        return false;
    }

    public boolean callBeforeActivate() {
        if (widget.isCallBeforeActivate() && hasPropertyChangeListener()) {
            return new ComponentServiceCaller(widget.getContent().getDisplayName(), widget.isLongRunning()) {

                @Override
                protected void doWork() throws Throwable {
                    componentService.beforePropertyActivate(getParameterName(), getSubProperties());
                    update();
                }
            }.call();
        }
        return false;
    }

    private void update() {
        NamedThing property = getSubProperties().getProperty(getParameterName());
        if (property != null && property instanceof Property) {
            List<?> values = ((Property) property).getPossibleValues();
            if (values != null) {
                this.setPossibleValues(values);
            }
        }
    }

    private boolean callValidate() {
        if (widget.isCallValidate() && hasPropertyChangeListener()) {
            return new ComponentServiceCaller(widget.getContent().getDisplayName(), widget.isLongRunning()) {

                @Override
                protected void doWork() throws Throwable {
                    componentService.validateProperty(getParameterName(), getSubProperties());
                }
            }.call();
        }
        return false;
    }

    private boolean callAfter() {
        if (widget.isCallAfter() && hasPropertyChangeListener()) {
            return new ComponentServiceCaller(widget.getContent().getDisplayName(), widget.isLongRunning()) {

                @Override
                protected void doWork() throws Throwable {
                    componentService.afterProperty(getParameterName(), getSubProperties());
                    updateSchema();
                }
            }.call();
        }
        return false;
    }

    private void updateSchema() {
        IElement element = this.getElement();
        if (element instanceof Node) {
            Node node = (Node) element;
            List<IMetadataTable> metadataList = node.getMetadataList();
            if (metadataList.size() > 0) {
                IMetadataTable oldTable = metadataList.get(0);
                String schemaPropertyName = oldTable.getAdditionalProperties().get(IComponentConstants.COMPONENT_SCHEMA_TAG);
                Object schemaObj = null;
                try {
                    schemaObj = ComponentsUtils.getGenericPropertyValue(rootProperties, schemaPropertyName);
                    if (schemaObj instanceof String) {
                        schemaObj = new Schema.Parser().parse((String) schemaObj);
                    }
                } catch (Exception e) {
                    // do nothing
                }
                if (schemaObj != null && schemaObj instanceof Schema) {
                    MetadataTable metadataTable = SchemaUtils.createSchema(oldTable.getTableName(), rootProperties,
                            schemaPropertyName);
                    IMetadataTable newTable = MetadataToolHelper.convert(metadataTable);
                    if (!newTable.sameMetadataAs(oldTable)) {
                        IElementParameter schemaParameter = node
                                .getElementParameterFromField(EParameterFieldType.SCHEMA_REFERENCE);
                        ChangeMetadataCommand cmd = new ChangeMetadataCommand(node, schemaParameter, oldTable, newTable, null);
                        IProcess process = node.getProcess();
                        if (process instanceof org.talend.designer.core.ui.editor.process.Process) {
                            CommandStack commandStack = ((org.talend.designer.core.ui.editor.process.Process) process)
                                    .getCommandStack();
                            commandStack.execute(cmd);
                        }
                    }
                }
            }
        }
    }

    private String getParameterName() {
        String paramName = getName();
        if (paramName.indexOf(IGenericConstants.EXP_SEPARATOR) != -1) {
            paramName = paramName.substring(paramName.lastIndexOf(IGenericConstants.EXP_SEPARATOR) + 1);
        }
        return paramName;
    }

    abstract class ComponentServiceCaller {

        private String title;

        private boolean isLongRuning;

        public ComponentServiceCaller(String title, boolean isLongRuning) {
            this.title = title;
            this.isLongRuning = isLongRuning;
        }

        public boolean call() {
            if (isLongRuning) {
                return new RunWithProgress(title) {

                    @Override
                    protected void toDo() throws Throwable {
                        doWork();
                    };

                }.run();
            } else {
                try {
                    doWork();
                } catch (Throwable e) {
                    ExceptionHandler.process(e);
                    return false;
                }
            }
            return true;
        }

        protected abstract void doWork() throws Throwable;

    }

    abstract class RunWithProgress {

        private String taskName;

        public RunWithProgress(String taskName) {
            this.taskName = taskName;
        }

        public boolean run() {
            final AtomicBoolean result = new AtomicBoolean();
            IRunnableWithProgress runnableWithProgress = new IRunnableWithProgress() {

                @Override
                public void run(final IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
                    monitor.beginTask(taskName, IProgressMonitor.UNKNOWN);
                    try {
                        toDo();
                    } catch (Throwable e) {
                        result.set(false);
                        throw new InvocationTargetException(e);
                    }
                    result.set(true);
                }
            };
            ProgressMonitorDialog dialog = new ProgressMonitorDialog(PlatformUI.getWorkbench().getDisplay().getActiveShell());
            try {
                dialog.run(true, true, runnableWithProgress);
            } catch (Exception e) {
                result.set(false);
                ExceptionHandler.process(e);
            }
            return result.get();
        }

        protected abstract void toDo() throws Throwable;

    }

    public List<?> getPossibleValues() {
        return this.possibleValues;
    }

    public void setPossibleValues(List<?> possibleValues) {
        this.possibleValues = possibleValues;
    }

    public Widget getWidget() {
        return this.widget;
    }
    
    private ComponentProperties getSubProperties() {
        return ComponentsUtils.getCurrentComponentProperties(rootProperties, getName());
    }
    
    public ComponentProperties getRootProperties() {
        return this.rootProperties;
    }

    public void setRootProperties(ComponentProperties rootProperties) {
        this.rootProperties = rootProperties;
    }

    public boolean isSupportContext() {
        return this.supportContext;
    }

    public void setSupportContext(boolean supportContext) {
        this.supportContext = supportContext;
    }

    public ComponentService getComponentService() {
        return this.componentService;
    }

    public void setComponentService(ComponentService componentService) {
        this.componentService = componentService;
    }

    public boolean isDrivedByForm() {
        return this.drivedByForm;
    }

    public void setDrivedByForm(boolean drivedByForm) {
        this.drivedByForm = drivedByForm;
    }
}
