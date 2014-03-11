package com.evolveum.midpoint.repo.sql.data.common;

import com.evolveum.midpoint.prism.PrismContext;
import com.evolveum.midpoint.repo.sql.query.definition.JaxbType;
import com.evolveum.midpoint.repo.sql.util.DtoTranslationException;
import com.evolveum.midpoint.repo.sql.util.RUtil;
import com.evolveum.midpoint.xml.ns._public.common.common_2a.ObjectType;
import com.evolveum.midpoint.xml.ns._public.common.common_2a.TriggerType;
import org.apache.commons.lang.Validate;
import org.hibernate.annotations.ForeignKey;
import org.hibernate.annotations.Index;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.xml.datatype.XMLGregorianCalendar;

@JaxbType(type = TriggerType.class)
@Entity
@ForeignKey(name = "fk_trigger")
@org.hibernate.annotations.Table(appliesTo = "m_trigger",
        indexes = {@Index(name = "iTriggerTimestamp", columnNames = RTrigger.C_TIMESTAMP)})
public class RTrigger extends RContainer {

    public static final String C_TIMESTAMP = "timestampValue";
    public static final String F_OWNER = "owner";

    private String handlerUri;
    private XMLGregorianCalendar timestamp;

    public RTrigger() {
        this(null);
    }

    public RTrigger(RObject owner) {
        setOwner(owner);
    }

    public String getHandlerUri() {
        return handlerUri;
    }

    @Column(name = C_TIMESTAMP)
    public XMLGregorianCalendar getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(XMLGregorianCalendar timestamp) {
        this.timestamp = timestamp;
    }

    public void setHandlerUri(String handlerUri) {
        this.handlerUri = handlerUri;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RTrigger that = (RTrigger) o;

        if (handlerUri != null ? !handlerUri.equals(that.handlerUri) :
                that.handlerUri != null) return false;
        if (timestamp != null ? !timestamp.equals(that.timestamp) :
                that.timestamp != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = handlerUri != null ? handlerUri.hashCode() : 0;
        result = 31 * result + (timestamp != null ? timestamp.hashCode() : 0);
        return result;
    }

    public static void copyToJAXB(RTrigger repo, TriggerType jaxb, PrismContext prismContext) throws
            DtoTranslationException {
        Validate.notNull(repo, "Repo object must not be null.");
        Validate.notNull(jaxb, "JAXB object must not be null.");

        jaxb.setId(RUtil.toLong(repo.getId()));

        jaxb.setHandlerUri(repo.getHandlerUri());
        jaxb.setTimestamp(repo.getTimestamp());

    }

    public static void copyFromJAXB(TriggerType jaxb, RTrigger repo, ObjectType parent,
                                    PrismContext prismContext) throws DtoTranslationException {
        Validate.notNull(repo, "Repo object must not be null.");
        Validate.notNull(jaxb, "JAXB object must not be null.");

        repo.setOwnerOid(parent.getOid());
        repo.setId(RUtil.toShort(jaxb.getId()));

        repo.setHandlerUri(jaxb.getHandlerUri());
        repo.setTimestamp(jaxb.getTimestamp());
    }

    public TriggerType toJAXB(PrismContext prismContext) throws DtoTranslationException {
        TriggerType object = new TriggerType();
        RTrigger.copyToJAXB(this, object, prismContext);
        return object;
    }
}
