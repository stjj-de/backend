import { list } from "@keystone-6/core"
import { allowAll } from "@keystone-6/core/access"
import { image, text } from "@keystone-6/core/fields"
import { isEditorPredicate } from "../helpers"

export const Picture = list({
  fields: {
    file: image({
      label: "Picture file",
      storage: "images"
    }),
    altText: text({
      label: "Alt text",
      validation: {
        isRequired: false
      }
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
      initialColumns: ["altText"]
    },
    labelField: "altText"
  }
})
