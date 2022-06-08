import { list } from "@keystone-6/core"
import { integer, text } from "@keystone-6/core/fields"
import { isEditorPredicate } from "../helpers"

export const Link = list({
  fields: {
    url: text({
      label: "URL",
      validation: {
        isRequired: true,
        match: {
          regex: new RegExp("^(([^:/?#]+):)?(//([^/?#]*))?([^?#]*)(\\?([^#]*))?(#(.*))?")
        }
      }
    }),
    text: text({
      label: "Text",
      validation: {
        isRequired: true
      }
    }),
    emoji: text({
      label: "Emoji",
      validation: {
        isRequired: true,
        match: {
          regex: new RegExp("(\u00a9|\u00ae|[\u2000-\u3300]|\ud83c[\ud000-\udfff]|\ud83d[\ud000-\udfff]|\ud83e[\ud000-\udfff])")
        }
      }
    }),
    priority: integer({
      label: "Priority",
      validation: {
        isRequired: true
      },
      defaultValue: 1
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
      initialColumns: ["url", "text"]
    },
    labelField: "url"
  }
})
