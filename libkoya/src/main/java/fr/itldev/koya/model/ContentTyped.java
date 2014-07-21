package fr.itldev.koya.model;

/**
 *
 *
 */
public interface ContentTyped {

    /**
     * Useful method to deserialize content.
     *
     * @return
     */
    public String getContentType();

    /**
     * Implemented for deserialization compatibility
     *
     * @param contentType
     */
    public void setContentType(String contentType);
}
