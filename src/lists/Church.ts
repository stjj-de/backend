import { list } from "@keystone-6/core"
import { image, text } from "@keystone-6/core/fields"
import { document } from "@keystone-6/fields-document"
import { isEditorPredicate } from "../helpers"

export const Church = list({
  fields: {
    name: text({
      label: "Name",
      validation: {
        isRequired: true
      }
    }),
    location: text({
      label: "Location",
      validation: {
        isRequired: true
      }
    }),
    description: document({
      label: "Description",
      formatting: {
        inlineMarks: {
          bold: true,
          italic: true,
          superscript: true,
          strikethrough: true
        }
      },
      links: true
    }),
    image: image({
      label: "Image"
    })
  },
  access: {
    operation: {
      create: isEditorPredicate,
      query: () => true,
      update: isEditorPredicate,
      delete: isEditorPredicate
    }
  },
  ui: {
    listView: {
      initialColumns: ["name", "location"],
      initialSort: {
        field: "name",
        direction: "ASC"
      }
    },
    labelField: "name"
  }
})
