package com.scholarsmanuscript.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserSettingsRequest {

    private String theme;
    private String font;
    private String voiceSetting;
}
