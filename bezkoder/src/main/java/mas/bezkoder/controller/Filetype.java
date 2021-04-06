package mas.bezkoder.controller;

public enum Filetype {

    CSS("string"),
    HTML("string"),
    JS("string"),
    JPEG("image"),
    JPG("image"),
    PNG("image"),
    GIF("image"),
    ICO("image"),
    MPEG("video"),
    MPG("video");

    private String description;

    Filetype(String s) {
        this.description = s;
    }

    public static String getDescription(String string) {
        for (Filetype ft: Filetype.values()) {
            if (ft.name().equalsIgnoreCase(string)) return ft.description;
        }
        return "other";
    }
}
