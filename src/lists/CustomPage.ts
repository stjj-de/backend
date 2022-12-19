import { list } from "@keystone-6/core"
import { text } from "@keystone-6/core/fields"
import { isAdminPredicate, isEditorPredicate, slug } from "../helpers"

export const CustomPage = list({
  fields: {
    title: text({
      label: "Title",
      validation: {
        isRequired: true
      }
    }),
    slug: slug(),
    content: text()
  },
  access: {
    operation: {
      create: isEditorPredicate,
      query: () => true,
      update: isEditorPredicate,
      delete: isAdminPredicate
    }
  },
  ui: {
    listView: {
      initialColumns: ["title"],
      initialSort: {
        field: "title",
        direction: "ASC"
      }
    },
    labelField: "title",
    hideDelete: ({ session }) => !isAdminPredicate({ session })
  }
})
