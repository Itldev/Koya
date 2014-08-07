package fr.itldev.koya.model.json;

/**
 *
 * Wraps boolean value.
 *
 *
 */
public class BooleanWrapper {

    private Boolean value;

    public Boolean getValue() {
        return value;
    }

    public void setValue(Boolean value) {
        this.value = value;
    }

    public BooleanWrapper(Boolean value) {
        this.value = value;
    }

    public BooleanWrapper() {
    }

    /**
     * Useful method to deserialize content.
     *
     * @return
     */
    public String getContentType() {
        return this.getClass().getCanonicalName();
    }

    /**
     * Implemented for deserialization compatibility
     *
     * @param contentType
     */
    public void setContentType(String contentType) {
    }

}
