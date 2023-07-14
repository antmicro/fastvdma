# -*- coding: utf-8 -*-

from datetime import datetime

from antmicro_sphinx_utils.defaults import (
    extensions,
    myst_enable_extensions,
    myst_fence_as_directive,
    antmicro_html,
    antmicro_latex
)

# -- General configuration -----------------------------------------------------

# General information about the project.
project = u'FastVDMA'
basic_filename = u'fastvdma'
authors = u'Antmicro'
copyright = f'{authors}, {datetime.now().year}'

version = ''
release = ''

sphinx_immaterial_override_builtin_admonitions = False
numfig = True

myst_substitutions = {
    "project": project
}

today_fmt = '%Y-%m-%d'

todo_include_todos=False

# -- Options for HTML output ---------------------------------------------------

html_theme = 'sphinx_immaterial'

html_last_updated_fmt = today_fmt

html_show_sphinx = False

(
    html_logo,
    html_theme_options,
    html_context
) = antmicro_html(pdf_url=f"{basic_filename}.pdf")

html_title = project

(
    latex_elements,
    latex_documents,
    latex_logo,
    latex_additional_files
) = antmicro_latex(basic_filename, authors, project)
