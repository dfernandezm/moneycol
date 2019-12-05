import React, { ReactComponentElement, useState, ReactNode } from 'react';
import './collapse.css';

import { ReactNodeArray } from 'prop-types';

type CollapseProps = {
  clickablePart : ReactNode,
  index: number
}

const Collapse: React.FC<CollapseProps> = ({clickablePart, index, children}) => {
  const [opened, setOpened] = useState(false)

  const onClick = (e: React.MouseEvent) => {
    setOpened(!opened)
  }

  return (
    <div className="wrap-collapsible">
      <input id="collapsible" className="toggle" type="checkbox"/>
      <label htmlFor="collapsible" className="lbl-toggle" onClick={onClick}>something {index}</label>
      <div className={!opened ? "collapsible-content" : "collapsible-content-opened"}>
        <div className="content-inner">
         {children}
        </div>
      </div>
    </div>
  );
}

export default Collapse;