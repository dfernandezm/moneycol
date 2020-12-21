import React, {ReactNode, useEffect, useRef} from 'react';
import PerfectScrollbar from 'react-perfect-scrollbar';
import {RouteComponentProps, useLocation, withRouter} from 'react-router-dom';

interface ScrollbarProps extends RouteComponentProps<any> {
  children: ReactNode;
  className: string;
  location: any;
  scrollToTop: boolean;
  staticContext: any;

  [x: string]: any;
}

const Scrollbar: React.FC<ScrollbarProps> = React.forwardRef((props, ref) => {
  const {
    children,
    scrollToTop,
    staticContext,
    className = '',
    ...others
  } = props;
  let _scrollBarRef = useRef<HTMLElement>(null);
  const {pathname} = useLocation();

  useEffect(() => {
    if (_scrollBarRef) {
      // @ts-ignore
      _scrollBarRef._container.scrollTop = 0;
    }
  }, [_scrollBarRef, pathname]);

  return (
    <PerfectScrollbar
      ref={ref => {
        // @ts-ignore
        _scrollBarRef = ref;
      }}
      {...others}
      className={className}>
      {children}
    </PerfectScrollbar>
  );
});

export default withRouter(Scrollbar);
