import { list } from "@keystone-6/core"
import { checkbox, password, text } from "@keystone-6/core/fields"
import { Session } from "../auth"
import { isAdminPredicate } from "../helpers"

export const User = list({
  fields: {
    username: text({
      label: "Username",
      isIndexed: "unique",
      validation: {
        isRequired: true,
        length: {
          max: 50
        }
      }
    }),
    firstName: text({
      label: "First name",
      validation: {
        isRequired: true
      }
    }),
    password: password(),
    isAdmin: checkbox({
      label: "Is administrator",
      ui: {
        itemView: {
          fieldMode: ({ session, item }) => (session as Session).data.id == item.id ? "read" : "edit"
        }
      }
    })
  },
  access: {
    operation: {
      create: isAdminPredicate,
      update: isAdminPredicate,
      delete: isAdminPredicate,
      query: isAdminPredicate
    }
  },
  ui: {
    isHidden: session => !isAdminPredicate((session)),
    listView: {
      initialColumns: ["username", "firstName", "isAdmin"],
      pageSize: 20,
      initialSort: {
        field: "username",
        direction: "ASC"
      }
    },
    labelField: "username",
    searchFields: ["username", "firstName"]
  }
})
