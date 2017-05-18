package com.fighter.wrapper.download;

import java.util.HashMap;
import java.util.Map;

class MimeTypes {


    private static final String COMMENT_PREFIX = "#";

    private final Map<String, MimeType> mimeTypes = new HashMap<>();
    private final Map<String, MimeType> extensions = new HashMap<>();

    private static MimeTypes singleton = null;
    private final static Object singletonMonitor = new Object();

    public MimeTypes() {
        load(MimeTypeDef.DEF);
    }


    /**
     * Get the default instance which is initialized with the built-in mime
     * types definitions on the first access to this method.
     * <p>
     * <p>This is thread-safe.
     *
     * @return default singleton instance with built-in mime types definitions
     */
    public static MimeTypes getInstance() {
        if (singleton == null) {
            synchronized (singletonMonitor) {
                if (singleton == null) {
                    singleton = new MimeTypes();
                }
            }
        }

        return singleton;
    }

    /**
     * Parse and register mime type definitions from given path.
     *
     * @param def Path of mime type definitions file to load and register
     * @return This instance of Mimetypes
     */
    public MimeTypes load(String def) {
        for (String line : def.split("\n")) {
            loadOne(line);
        }
        return this;
    }

    /**
     * Load and register a single line that starts with the mime type proceeded
     * by any number of whitespaces, then a whitespace separated list of
     * valid extensions for that mime type.
     *
     * @param def Single mime type definition to load and register
     * @return This instance of Mimetypes
     */
    public MimeTypes loadOne(String def) {
        if (def.startsWith(COMMENT_PREFIX)) {
            return this;
        }

        String[] halves = def.toLowerCase().split("\\s", 2);

        MimeType mimeType = new MimeType(halves[0], halves[1].trim().split("\\s"));
        return register(mimeType);
    }

    /**
     * Register the given {@link MimeType} so it can be looked up later by mime
     * type and/or extension.
     *
     * @param mimeType MimeType instance to register
     * @return This instance of Mimetypes
     */
    public MimeTypes register(MimeType mimeType) {
        mimeTypes.put(mimeType.getMimeType(), mimeType);
        for (String ext : mimeType.getExtensions()) {
            extensions.put(ext, mimeType);
        }
        return this;
    }

    /**
     * Get a @{link MimeType} instance for the given mime type identifier from
     * the loaded mime type definitions.
     *
     * @param mimeType lower-case mime type identifier string
     * @return Instance of MimeType for the given mime type identifier or null
     * if none was found
     */
    public MimeType getByType(String mimeType) {
        return mimeTypes.get(mimeType);
    }

    /**
     * Get a @{link MimeType} instance for the given extension from the loaded
     * mime type definitions.
     *
     * @param extension lower-case extension
     * @return Instance of MimeType for the given ext or null if none was found
     */
    public MimeType getByExtension(String extension) {
        return extensions.get(extension);
    }
}
