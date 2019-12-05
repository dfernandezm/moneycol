import React from 'react';

type LabelProps = {
    text: string
}

const Label: React.FC<LabelProps> = ({text}) => {
 return <label htmlFor="collapsible" className="lbl-toggle">{text}</label>;
}

export default Label;