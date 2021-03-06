package views.admin.programs;

import static j2html.TagCreator.body;
import static j2html.TagCreator.div;
import static j2html.TagCreator.form;
import static j2html.TagCreator.h1;

import com.google.inject.Inject;
import play.mvc.Http.Request;
import play.twirl.api.Content;
import views.BaseHtmlView;
import views.admin.AdminLayout;

public final class ProgramNewOneView extends BaseHtmlView {
  private final AdminLayout layout;

  @Inject
  public ProgramNewOneView(AdminLayout layout) {
    this.layout = layout;
  }

  public Content render(Request request) {
    return layout.render(
        body(
            h1("Create a new Program"),
            div(
                form(
                        makeCsrfTokenInputTag(request),
                        div(textField("name", "Program Name")),
                        div(textField("description", "Program Description")),
                        submitButton("Create"))
                    .withMethod("post")
                    .withAction(controllers.admin.routes.AdminProgramController.index().url()))));
  }
}
