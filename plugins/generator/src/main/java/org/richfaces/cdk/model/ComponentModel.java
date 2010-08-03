package org.richfaces.cdk.model;

import org.richfaces.cdk.model.RendererModel.Type;

/**
 * That class represents JSF component in the CDK.
 * That is mapped to faces-config "component" element.
 * @author asmirnov@exadel.com
 *
 */
public final class ComponentModel extends ModelElementBase implements ModelElement<ComponentModel> {

    private static final long serialVersionUID = 2297349356280370771L;

    /**
     * <p class="changed_added_4_0">
     * Facets recognised by the component
     * </p>
     */
    private final ModelCollection<FacetModel> facets = ModelSet.<FacetModel>create();

    /**
     * <p class="changed_added_4_0">
     * Application level events fired by the component
     * </p>
     */
    private final ModelCollection<EventModel> events = ModelSet.<EventModel>create();

    /**
     * <p class="changed_added_4_0">
     * JsfRenderer for the final component. This is bidirectional many to many
     * relation.
     * </p>
     */
    private String family;
    
    private RendererModel.Type rendererType;

    public ComponentModel(FacesId key) {
        this.setId(key);
    }
    
    public ComponentModel() {
        
    }

    public <R,D> R accept(Visitor<R,D> visitor, D data) {
        return visitor.visitComponent(this,data);

//         TODO ??? see at render kit
//        for (RendererModel rendererType : renderers) {
//            rendererType.accept(visitor);
//        }
    }

    @Override
    public void merge(ComponentModel otherComponent) {
        //merge facets, renderers, events ...
        ComponentLibrary.merge(getAttributes(), otherComponent.getAttributes());
        ComponentLibrary.merge(getFacets(), otherComponent.getFacets());
        ComponentLibrary.merge(getEvents(), otherComponent.getEvents());
        ComponentLibrary.merge(this, otherComponent);
        this.setGenerate(this.isGenerate() || otherComponent.isGenerate());
    }

    @Override
    public boolean same(ComponentModel other) {
        if (null != getType() && null != other.getType()) {
            // Both types not null, compare them.
            return getType().equals(other.getType());
        }
        
        // one or both types are null, compare classes.
        return null != getTargetClass() && getTargetClass().equals(other.getTargetClass());
    }
    /**
     * <p class="changed_added_4_0">Delegeted to setId</p>
     * @param type the type to set
     * @deprecated Use {@link ModelElementBase#setId(FacesId)} instead.
     */
    public void setType(FacesId type) {
        setId(type);
    }

    /**
     * <p class="changed_added_4_0"></p>
     * @return
     * @deprecated Use {@link ModelElementBase#getId()} instead.
     */
    public FacesId getType() {
        return getId();
    }


    /**
     * <p class="changed_added_4_0"></p>
     * @return the rendererType
     */
    @Merge
    public Type getRendererType() {
        return this.rendererType;
    }

    /**
     * <p class="changed_added_4_0"></p>
     * @param renderer the rendererType to set
     */
    public void setRendererType(Type renderer) {
        this.rendererType = renderer;
    }

    /**
     * <p class="changed_added_4_0">
     * Reepresent a component family. In the faces-config element that property encoded as
     * <component><component-extension><cdk:component-family>....
     * </p>
     *
     * @return the family
     */
    @Merge
    public String getFamily() {
        return family;
    }

    /**
     * <p class="changed_added_4_0">
     * </p>
     *
     * @param family
     *            the family to set
     */
    public void setFamily(String family) {
        this.family = family;
    }

    /**
     * <p class="changed_added_4_0">Alias for TargetClass.
     * </p>
     *
     * @return the componentClass
     * @deprecated
     */
    @Merge
    public ClassName getComponentClass() {
        return getTargetClass();
    }

    /**
     * <p class="changed_added_4_0"></p>
     * @param name
     * @deprecated
     */
    public void setComponentClass(ClassName name) {
        setTargetClass(name);
    }
    
    /**
     * <p class="changed_added_4_0"></p>
     * @return the facets
     */
    public ModelCollection<FacetModel> getFacets() {
        return facets;
    }

    public FacetModel getFacet(final String name) {
        return facets.find(new Named.NamePredicate(name));
    }
    
    public FacetModel getOrCreateFacet(String name) {
        FacetModel facet = getFacet(name);
        if (null == facet) { 
            facet = new FacetModel();
            facet.setName(name);
            facets.add(facet);
        }
        return facet;
    }

    /**
     * <p class="changed_added_4_0"></p>
     * @return the events
     */
    public ModelCollection<EventModel> getEvents() {
        return events;
    }

    public EventModel addEvent(String className) {

        // TODO - use a single events collection from library.
        EventModel event = new EventModel();
        event.setType(new ClassName(className));
        events.add(event);
        return event;
    }

    @Override
    public String toString() {
        return "Component {type: " + getType() + ", family: " + getFamily() + "}";
    }
}
