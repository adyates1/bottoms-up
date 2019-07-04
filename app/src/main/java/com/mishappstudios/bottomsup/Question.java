package com.mishappstudios.bottomsup;

import java.io.Serializable;

/**
 * Class to represent a Question
 */
public class Question implements Serializable {
    // Private fields
    private static final long serialVersionUID = 3170247878268182624L;
    private String content;

    /**
     * Class constructor
     *
     * @param contentP    the question content
     */
    public Question(String contentP) {
        this.content = contentP;
    }

    /**
     * Gets the question content
     *
     * @return
     */
    public String getqContent() {
        return content;
    }


}