package views.admin.programs;

import static j2html.TagCreator.a;
import static j2html.TagCreator.body;
import static j2html.TagCreator.div;
import static j2html.TagCreator.form;
import static j2html.TagCreator.join;

import com.google.inject.Inject;
import play.mvc.Http.Request;
import play.twirl.api.Content;
import services.program.ProgramDefinition;
import views.BaseHtmlView;
import views.admin.AdminLayout;

public class ProgramEditView extends BaseHtmlView {
  private final AdminLayout layout;

  @Inject
  public ProgramEditView(AdminLayout layout) {
    this.layout = layout;
  }

  public Content render(Request request, ProgramDefinition program) {
    return layout.render(
        body(
            div(join("Edit program:", program.name())),
            div(
                form(
                        makeCsrfTokenInputTag(request),
                        div(textFieldWithValue("name", "Program Name", program.name())),
                        div(
                            textFieldWithValue(
                                "description", "Program Description", program.description())),
                        submitButton("Save"))
                    .withMethod("post")
                    .withAction(
                        controllers.admin.routes.AdminProgramController.update(program.id())
                            .url())),
            div(
                a().withText("Manage Questions")
                    .withHref(
                        controllers.admin.routes.AdminProgramBlocksController.index(program.id())
                            .url()))));
  }
}
