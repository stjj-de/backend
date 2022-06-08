import React from "react";
import { component, fields, NotEditable } from "@keystone-6/fields-document/component-blocks"
import { HydratedRelationshipData } from "@keystone-6/fields-document/dist/declarations/src/DocumentEditor/component-blocks/api"

export const componentBlocks = {
  Picture: component({
    label: "Picture",
    // component: props => <img src={props.picture.file.url} alt={props.picture.altText}/>,
    component: props => {
      if (props.picture.value === null) {
        return <NotEditable>
          <div style={{
            borderRadius: "10px",
            width: "200px",
            height: "150px",
            backgroundColor: "rgba(0,0,0,0.1)"
          }}/>
        </NotEditable>
      } else {
        const picture = props.picture.value.data as unknown as { file: { url: string }; altText: string }
        return <NotEditable>
          <img style={{ maxWidth: "200px", display: "block" }} src={picture.file.url} alt={picture.altText}/>
        </NotEditable>
      }
    },
    chromeless: false,
    props: {
      picture: fields.relationship({
        label: "Picture",
        many: false,
        listKey: "Picture",
        selection: "altText file { url }"
      }),
      caption: fields.text({
        label: "Caption",
        defaultValue: ""
      })
    }
  })
};
