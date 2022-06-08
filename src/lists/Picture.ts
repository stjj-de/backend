import { list } from "@keystone-6/core"
import { image, text } from "@keystone-6/core/fields"

export const Picture = list({
  fields: {
    file: image({
      label: "Picture file"
    }),
    altText: text({
      label: "Alt text",
      validation: {
        isRequired: false
      }
    })
  },
  ui: {
    listView: {
      initialColumns: ["altText"]
    },
    labelField: "altText"
  }
})
