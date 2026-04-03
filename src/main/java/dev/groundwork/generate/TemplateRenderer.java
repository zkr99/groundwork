package dev.groundwork.generate;

import com.samskivert.mustache.Mustache;
import java.util.Map;

public final class TemplateRenderer {
    public String render(String template, Map<String, ?> context) {
        return Mustache.compiler()
                .escapeHTML(false)
                .compile(template)
                .execute(context);
    }
}
