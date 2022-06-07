import { list } from '@keystone-6/core';

import {
  text,
  relationship,
  password,
  timestamp, checkbox, image, file, integer
} from "@keystone-6/core/fields"
import { document } from '@keystone-6/fields-document';

import { Lists } from '.keystone/types';
import { Session } from "./auth"

const isAdminPredicate = ({ session }: { session?: Session }) => session?.data?.isAdmin == true
const isEditorPredicate = ({ session }: { session?: Session }) => session?.data?.id !== undefined

const slug = (label: string = "Slug") => text({
  label,
  isIndexed: "unique",
  validation: {
    isRequired: true,
    match: {
      regex: /^[a-z0-9]+(?:-[a-z0-9]+)*$/
    },
    length: {
      max: 100
    }
  },
  access: {
    update: isAdminPredicate
  }
})

export const lists: Lists = {
  User: list({
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
      forename: text({
        label: "Forename",
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
        initialColumns: ["username", "forename", "isAdmin"],
        pageSize: 20,
        initialSort: {
          field: "username",
          direction: "ASC"
        }
      },
      labelField: "username",
      searchFields: ["username", "forename"]
    }
  }),

  Person: list({
    fields: {
      displayName: text({
        label: "Display name",
        validation: {
          isRequired: true
        }
      }),
      image: image({
        label: "Image"
      }),
      role: text({
        label: "Role",
        validation: {
          isRequired: true
        }
      }),
      priority: integer({
        label: "Priority",
        defaultValue: 1,
        isFilterable: false,
        isIndexed: true
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
  }),

  Church: list({
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
  }),

  ChurchServiceDate: list({
    fields: {
      shortDescription: text({
        label: "Short description",
        validation: {
          isRequired: true,
          length: {
            max: 100
          }
        }
      }),
      longDescription: document({
        label: "Long description",
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
      date: timestamp({
        label: "Date",
        validation: {
          isRequired: true
        }
      }),
      livestreamPlanned: checkbox({
        label: "Livestream is planned",
        defaultValue: false
      }),
      church: relationship({
        label: "Church",
        ref: "Church",
        many: false
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
        initialColumns: ["date", "shortDescription", "livestreamPlanned"]
      },
      labelField: "shortDescription"
    }
  }),

  Link: list({
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
  }),

  CustomPage: list({
    fields: {
      title: text({
        label: "Title",
        validation: {
          isRequired: true
        }
      }),
      slug: slug(),
      content: document({
        label: "Content",
        formatting: {
          listTypes: true,
          blockTypes: {
            blockquote: true
          },
          headingLevels: [1, 2, 3],
          inlineMarks: {
            bold: true,
            italic: true,
            superscript: true,
            strikethrough: true
          }
        },
        links: true
      })
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
  }),

  Post: list({
    fields: {
      title: text({
        label: "Title",
        validation: {
          isRequired: true
        }
      }),
      publicationDate: timestamp({
        label: "Publication date",
        validation: {
          isRequired: false
        }
      }),
      slug: slug(),
      authors: relationship({
        label: "Authors",
        ref: "Person",
        many: true
      }),
      featured: checkbox({
        label: "Is featured"
      }),
      content: document({
        label: "Content",
        formatting: {
          listTypes: true,
          blockTypes: {
            blockquote: true
          },
          headingLevels: [1, 2, 3],
          inlineMarks: {
            bold: true,
            italic: true,
            superscript: true,
            strikethrough: true
          }
        },
        links: true
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
        initialColumns: ["title", "authors"],
        initialSort: {
          field: "publicationDate",
          direction: "DESC"
        }
      },
      labelField: "title",
      searchFields: ["title", "slug"]
    }
  }),

  Video: list({
    fields: {
      title: text({
        label: "Title",
        validation: {
          isRequired: true
        }
      }),
      youtubeVideoId: text({
        label: "YouTube Video ID",
        validation: {
          isRequired: true
        },
        isOrderable: false,
        isFilterable: false
      }),
      publicationDate: timestamp({
        label: "Publication date",
        validation: {
          isRequired: true
        }
      }),
      description: document({
        label: "Description",
        formatting: {
          listTypes: true,
          blockTypes: {
            blockquote: true
          },
          headingLevels: [1, 2, 3],
          inlineMarks: {
            bold: true,
            italic: true,
            superscript: true,
            strikethrough: true
          }
        },
        links: true
      })
    }
  }),

  SettingsSingleton: list({
    fields: {
      livestreamVideoId: text({
        label: "Livestream video ID",
        validation: {
          match: {
            regex: /^[a-zA-Z0-9_-]*$/,
            explanation: "YouTube Video ID"
          },
          isRequired: false
        }
      }),
      acolyteSchedule: file({
        label: "Acolyte schedule"
      }),
      pastors: relationship({
        label: "Pastors",
        ref: "Person",
        many: true
      }),
      homePageLinks: relationship({
        label: "Home page links",
        ref: "Link",
        many: true
      }),
      officeSectionContent: document({
        label: "»Pfarrbüro« section",
        formatting: {
          listTypes: true,
          blockTypes: {
            blockquote: true
          },
          headingLevels: [2, 3],
          inlineMarks: {
            bold: true,
            italic: true,
            superscript: true,
            strikethrough: true
          }
        },
        links: true
      })
    },
    access: {
      operation: {
        create: async ({ session, context }) => isAdminPredicate({ session }) && (await context.db.SettingsSingleton.count()) == 0,
        query: () => true,
        update: isEditorPredicate,
        delete: () => false
      }
    }
  })
};