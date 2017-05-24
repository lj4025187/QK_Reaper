package com.fighter.reaper.sample.model;


import static com.fighter.reaper.sample.config.SampleConfig.TEXT_AD_TYPE;

public class TextItem extends BaseItem {

    private String mText;

    public TextItem(String text) {
        super(TEXT_AD_TYPE);
        mText = text;
    }

    public String getText() {
        return mText;
    }
}
