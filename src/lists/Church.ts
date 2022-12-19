import { list } from "@keystone-6/core"
import { image, text } from "@keystone-6/core/fields"
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
    description: text(),
    image: image({
      label: "Image",
      storage: "images"
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
