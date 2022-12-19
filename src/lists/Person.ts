import { list } from "@keystone-6/core"
import { image, integer, text } from "@keystone-6/core/fields"
import { isEditorPredicate } from "../helpers"

export const Person = list({
  fields: {
    displayName: text({
      label: "Display name",
      validation: {
        isRequired: true
      }
    }),
    priority: integer({
      label: "Priority",
      defaultValue: 1,
      isFilterable: false,
      isIndexed: true
    }),
    image: image({
      label: "Image",
      storage: "images"
    }),
    role: text({
      label: "Role",
      validation: {
        isRequired: true
      }
    }),
    telephoneNumber: text({
      label: "Telephone number",
      validation: {
        isRequired: false,
        match: {
          regex: /^(|\+[0-9]([0-9] ?)*)$/
        }
      }
    }),
    emailAddress: text({
      label: "Email address",
      validation: {
        isRequired: false,
        match: {
          regex: /^(|[^@]+@[^@]+\.[^@]+)$/
        }
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
      initialColumns: ["displayName", "role"],
      initialSort: {
        field: "priority",
        direction: "ASC"
      }
    },
    labelField: "displayName",
    searchFields: ["role", "displayName"]
  }
})
