import { useSwipeable, SwipeableHandlers } from "react-swipeable";

export const useSwipeableItem = (
  id: number,
  onSwipeLeft: (id: number) => void,
  onSwipeRight: () => void
): SwipeableHandlers => {
  return useSwipeable({
    onSwipedLeft: () => onSwipeLeft(id),
    onSwipedRight: onSwipeRight,
    preventScrollOnSwipe: true,
    trackMouse: true,
  });
};