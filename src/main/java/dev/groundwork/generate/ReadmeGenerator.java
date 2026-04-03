package dev.groundwork.generate;

import dev.groundwork.scaffold.ProjectRequest;
import dev.groundwork.template.Template;

public final class ReadmeGenerator {
    private static final String README_TEMPLATE = """
            # {{project_name}}

            {{description}}

            ## Source Template

            - Template: `{{template_name}}`
            - Reference repositories: {{references_count}}

            ## Reference Repositories

            {{references_bullets}}

            ## Starter Directories

            {{directories_bullets}}

            ## Next Steps

            - Review `CLAUDE.md` for project-specific guidance.
            - Inspect `_reference/` for implementation patterns worth reusing.
            - Replace placeholder docs and directories with real project code.
            """;

    private final TemplateRenderer renderer;
    private final RenderContextFactory contextFactory;

    public ReadmeGenerator(TemplateRenderer renderer, RenderContextFactory contextFactory) {
        this.renderer = renderer;
        this.contextFactory = contextFactory;
    }

    public String generate(Template template, ProjectRequest request) {
        return renderer.render(README_TEMPLATE, contextFactory.create(template, request));
    }
}
