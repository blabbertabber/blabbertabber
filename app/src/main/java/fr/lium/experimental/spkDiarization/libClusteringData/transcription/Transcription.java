package fr.lium.experimental.spkDiarization.libClusteringData.transcription;

public class Transcription implements Cloneable {
    protected LinkSet linkSet;
    protected EntitySet entitySet;

    public Transcription() {
        super();
        linkSet = new LinkSet(-1);
        entitySet = new EntitySet();
    }

    /**
     * @return the linkSet
     */
    public LinkSet getLinkSet() {
        return linkSet;
    }

    /**
     * @param linkSet the linkSet to set
     */
    public void setLinkSet(LinkSet linkSet) {
        this.linkSet = linkSet;
    }

    /**
     * @return the entitySet
     */
    public EntitySet getEntitySet() {
        return entitySet;
    }

    /**
     * @param entitySet the entitySet to set
     */
    public void setEntitySet(EntitySet entitySet) {
        this.entitySet = entitySet;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#clone()
     */
    @Override
    public Object clone() throws CloneNotSupportedException {
        Transcription result = new Transcription();
        result.linkSet = (LinkSet) linkSet.clone();
        result.entitySet = (EntitySet) entitySet.clone();
        return result;
    }


}
